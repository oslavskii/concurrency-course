package course.concurrency.exams.refactoring;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.*;
import java.util.stream.Collectors;


public class MountTableRefresherService {

    private Others.RouterStore routerStore = new Others.RouterStore();
    private long cacheUpdateTimeout;

    /**
     * All router admin clients cached. So no need to create the client again and
     * again. Router admin address(host:port) is used as key to cache RouterClient
     * objects.
     */
    private Others.LoadingCache<String, Others.RouterClient> routerClientsCache;

    /**
     * Removes expired RouterClient from routerClientsCache.
     */
    private ScheduledExecutorService clientCacheCleanerScheduler;

    private ExecutorService mountTableRefresher;

    private final Others.MountTableManagerBuilder mountTableManagerBuilder;

    public MountTableRefresherService(Others.MountTableManagerBuilder builder) {
        this.mountTableManagerBuilder = builder;
    }

    public void serviceInit()  {
        long routerClientMaxLiveTime = 15L;
        this.cacheUpdateTimeout = 10L;
        routerClientsCache = new Others.LoadingCache<String, Others.RouterClient>();
        routerStore.getCachedRecords().stream().map(Others.RouterState::getAdminAddress)
                .forEach(addr -> routerClientsCache.add(addr, new Others.RouterClient()));

        initClientCacheCleaner(routerClientMaxLiveTime);
        initMountTableRefresher();
    }

    public void serviceStop() {
        clientCacheCleanerScheduler.shutdown();
        // remove and close all admin clients
        routerClientsCache.cleanUp();
        mountTableRefresher.shutdown();
    }

    private void initClientCacheCleaner(long routerClientMaxLiveTime) {
        ThreadFactory tf = new ThreadFactory() {
            @Override
            public Thread newThread(Runnable r) {
                Thread t = new Thread();
                t.setName("MountTableRefresh_ClientsCacheCleaner");
                t.setDaemon(true);
                return t;
            }
        };

        clientCacheCleanerScheduler =
                Executors.newSingleThreadScheduledExecutor(tf);
        /*
         * When cleanUp() method is called, expired RouterClient will be removed and
         * closed.
         */
        clientCacheCleanerScheduler.scheduleWithFixedDelay(
                () -> routerClientsCache.cleanUp(), routerClientMaxLiveTime,
                routerClientMaxLiveTime, TimeUnit.MILLISECONDS);
    }

    private void initMountTableRefresher() {
        ThreadFactory tf = new ThreadFactory() {
            @Override
            public Thread newThread(Runnable task) {
                Thread t = new Thread(task);
                t.setName("MountTableRefresh_");
                t.setDaemon(true);
                return t;
            }
        };
        mountTableRefresher = Executors.newCachedThreadPool(tf);
    }

    /**
     * Refresh mount table cache of this router as well as all other routers.
     */
    public void refresh()  {

        List<Others.RouterState> cachedRecords = routerStore.getCachedRecords();
        List<MountTableRefresherTask> refreshTasks = new ArrayList<>();
        for (Others.RouterState routerState : cachedRecords) {
            String adminAddress = routerState.getAdminAddress();
            if (adminAddress == null || adminAddress.length() == 0) {
                // this router has not enabled router admin.
                continue;
            }
            if (isLocalAdmin(adminAddress)) {
                /*
                 * Local router's cache update does not require RPC call, so no need for
                 * RouterClient
                 */
                refreshTasks.add(getLocalRefresher(adminAddress));
            } else {
                refreshTasks.add(new MountTableRefresherTask(
                        mountTableManagerBuilder.build(adminAddress), adminAddress));
            }
        }
        if (!refreshTasks.isEmpty()) {
            invokeRefresh(refreshTasks);
        }
    }

    protected MountTableRefresherTask getLocalRefresher(String adminAddress) {
        return new MountTableRefresherTask(mountTableManagerBuilder.build("local"), adminAddress);
    }

    private void removeFromCache(String adminAddress) {
        routerClientsCache.invalidate(adminAddress);
    }

    private void invokeRefresh(List<MountTableRefresherTask> tasks) {
        try {
            var cfs = tasks.stream()
                    .map(it -> CompletableFuture
                            .supplyAsync(it::call, mountTableRefresher)
                            .exceptionally((t) -> null)
                    )
                    .collect(Collectors.toList());
            Long completed = CompletableFuture.allOf(cfs.toArray(CompletableFuture[]::new))
                    .completeOnTimeout(null, cacheUpdateTimeout, TimeUnit.MILLISECONDS)
                    .thenApply(v -> cfs.stream()
                            .filter(CompletableFuture::isDone)
                            .map(CompletableFuture::join)
                            .filter(Objects::nonNull)
                            .count()
                    ).get();
            boolean allReqCompleted = tasks.size() == completed;
            if (!allReqCompleted) {
                log("Not all router admins updated their cache");
            }
        } catch (InterruptedException e) {
            log("Mount table cache refresher was interrupted.");
        } catch (ExecutionException e) {
            throw new RuntimeException(e);
        }
        logResult(tasks);
    }

    private boolean isLocalAdmin(String adminAddress) {
        return adminAddress.contains("local");
    }

    private void logResult(List<MountTableRefresherTask> refreshTasks) {
        int successCount = 0;
        int failureCount = 0;
        for (MountTableRefresherTask mountTableRefreshTask : refreshTasks) {
            if (mountTableRefreshTask.isSuccess()) {
                successCount++;
            } else {
                failureCount++;
                // remove RouterClient from cache so that new client is created
                removeFromCache(mountTableRefreshTask.getAdminAddress());
            }
        }
        log(String.format(
                "Mount table entries cache refresh successCount=%d,failureCount=%d",
                successCount, failureCount));
    }

    public void log(String message) {
        System.out.println(message);
    }

    public void setCacheUpdateTimeout(long cacheUpdateTimeout) {
        this.cacheUpdateTimeout = cacheUpdateTimeout;
    }
    public void setRouterClientsCache(Others.LoadingCache cache) {
        this.routerClientsCache = cache;
    }

    public void setRouterStore(Others.RouterStore routerStore) {
        this.routerStore = routerStore;
    }
}