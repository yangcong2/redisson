package org.redisson;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import org.junit.Assert;
import org.junit.Test;
import org.redisson.core.RCountDownLatch;

public class RedissonCountDownLatchTest {

    @Test
    public void testAwaitTimeout() throws InterruptedException {
        Redisson redisson = Redisson.create();

        ExecutorService executor = Executors.newFixedThreadPool(2);

        final RCountDownLatch latch = redisson.getCountDownLatch("latch1");
        latch.trySetCount(1);

        executor.execute(new Runnable() {
            @Override
            public void run() {
                try {
                    Thread.sleep(500);
                } catch (InterruptedException e) {
                    Assert.fail();
                }
                latch.countDown();
            }
        });

        executor.execute(new Runnable() {
            @Override
            public void run() {
                try {
                    Assert.assertEquals(1, latch.getCount());
                    boolean res = latch.await(550, TimeUnit.MILLISECONDS);
                    Assert.assertTrue(res);
                } catch (InterruptedException e) {
                    Assert.fail();
                }
            }
        });

        executor.shutdown();
        executor.awaitTermination(10, TimeUnit.SECONDS);

        redisson.shutdown();
    }

    @Test
    public void testAwaitTimeoutFail() throws InterruptedException {
        Redisson redisson = Redisson.create();

        ExecutorService executor = Executors.newFixedThreadPool(2);

        final RCountDownLatch latch = redisson.getCountDownLatch("latch1");
        latch.trySetCount(1);

        executor.execute(new Runnable() {
            @Override
            public void run() {
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    Assert.fail();
                }
                latch.countDown();
            }
        });

        executor.execute(new Runnable() {
            @Override
            public void run() {
                try {
                    Assert.assertEquals(1, latch.getCount());
                    boolean res = latch.await(500, TimeUnit.MILLISECONDS);
                    Assert.assertFalse(res);
                } catch (InterruptedException e) {
                    Assert.fail();
                }
            }
        });

        executor.shutdown();
        executor.awaitTermination(10, TimeUnit.SECONDS);

        redisson.shutdown();
    }

    @Test
    public void testCountDown() throws InterruptedException {
        Redisson redisson = Redisson.create();
        RCountDownLatch latch = redisson.getCountDownLatch("latch");
        latch.trySetCount(1);
        Assert.assertEquals(1, latch.getCount());
        latch.countDown();
        Assert.assertEquals(0, latch.getCount());
        latch.await();
        latch.countDown();
        Assert.assertEquals(0, latch.getCount());
        latch.await();
        latch.countDown();
        Assert.assertEquals(0, latch.getCount());
        latch.await();

        RCountDownLatch latch1 = redisson.getCountDownLatch("latch1");
        latch1.trySetCount(1);
        latch1.countDown();
        Assert.assertEquals(0, latch.getCount());
        latch1.countDown();
        Assert.assertEquals(0, latch.getCount());
        latch1.await();

        RCountDownLatch latch2 = redisson.getCountDownLatch("latch2");
        latch2.trySetCount(1);
        latch2.countDown();
        latch2.await();
        latch2.await();

        RCountDownLatch latch3 = redisson.getCountDownLatch("latch3");
        Assert.assertEquals(0, latch.getCount());
        latch3.await();

        RCountDownLatch latch4 = redisson.getCountDownLatch("latch4");
        Assert.assertEquals(0, latch.getCount());
        latch4.countDown();
        Assert.assertEquals(0, latch.getCount());
        latch4.await();

        redisson.shutdown();
    }

}
