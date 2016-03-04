package com.gxl.shark.benchmark;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.CountDownLatch;

import javax.annotation.Resource;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.gxl.shark.core.shard.SharkJdbcTemplate;

/**
 * shark benchmark
 * 
 * @author gaoxianglong
 * 
 * @version 1.0.0
 */
public class Benchmark {
	private int threadSize;
	private int taskSize;
	private SharkJdbcTemplate jdbcTemplate;
	private List<Integer> keys;
	private static long count = 0;
	private static int num = 0;
	@Resource
	private BenchmarkMapper benchmarkMapper;
	private Logger logger = LoggerFactory.getLogger(Benchmark.class);

	private Benchmark(int threadSize, int taskSize, SharkJdbcTemplate jdbcTemplate) {
		logger.info("### welcome to use shark-benchmark ###");
		logger.debug("threadSize-->" + threadSize + "\ntaskSize-->" + taskSize);
		this.threadSize = threadSize;
		this.taskSize = taskSize;
		this.jdbcTemplate = jdbcTemplate;
	}

	public void init() {
		createKey();
		run(true);
		run(false);
	}

	/**
	 * 开始执行基准测试
	 * 
	 * @author gaoxianglong
	 */
	public void run(final boolean type) {
		final CountDownLatch countDownLatch = new CountDownLatch(threadSize);
		long before = System.currentTimeMillis();
		for (int i = 0; i < threadSize; i++) {
			new Thread() {
				public @Override void run() {
					for (int i = 0; i < taskSize / threadSize; i++) {
						long before2 = System.currentTimeMillis();
						if (type) {
							jdbcTemplate.update("insert into benchmark(b_id) values(" + keys.get(num) + ")");
						} else {
							jdbcTemplate.query("select b_id from benchmark where b_id = " + keys.get(num) + "",
									benchmarkMapper);
						}
						count += (System.currentTimeMillis() - before2);
						num++;
					}
					countDownLatch.countDown();
				}
			}.start();
		}
		try {
			countDownLatch.await();
			long endTime = (System.currentTimeMillis() - before) / 1000;
			logger.info("shark-sharding,总共" + (type ? "写入" : "读取") + "耗时-->" + endTime + "s");
			logger.info("shark-sharding,每秒" + (type ? "写入" : "读取") + "次数-->" + ((double) taskSize / endTime));
			logger.info("shark-sharding,平均" + (type ? "写入" : "读取") + "耗时-->" + (count / taskSize) + "ms");
		} catch (InterruptedException e) {
			e.printStackTrace();
		} finally {
			if (0 != count)
				count = 0;
			if (0 != num)
				num = 0;
		}
	}

	/**
	 * 生成路由条件
	 * 
	 * @author gaoxianglong
	 */
	public void createKey() {
		keys = new ArrayList<Integer>();
		for (int i = 0; i < taskSize; i++)
			keys.add(Math.abs(UUID.randomUUID().toString().hashCode()));
	}
}