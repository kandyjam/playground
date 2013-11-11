package ch.rasc.reactorsandbox.samples;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import reactor.core.Environment;
import reactor.core.processor.Processor;
import reactor.core.processor.spec.ProcessorSpec;
import reactor.function.Consumer;
import reactor.function.Supplier;
import reactor.io.Buffer;

/**
 * @author Jon Brisbin
 */
public class ProcessorSamples {

	static final Logger LOG = LoggerFactory.getLogger(ProcessorSamples.class);

	static final Environment ENV = new Environment();

	static final int runs = 10000000;

	public static void main(String... args) throws Exception {
		final CountDownLatch latch = new CountDownLatch(runs);
		final AtomicLong sum = new AtomicLong();

		Processor<Buffer> proc = new ProcessorSpec<Buffer>().singleThreadedProducer().dataBufferSize(1024 * 16)
				.dataSupplier(new Supplier<Buffer>() {
					@Override
					public Buffer get() {
						return new Buffer(4, true);
					}
				}).consume(new Consumer<Buffer>() {
					@Override
					public void accept(Buffer buff) {
						sum.addAndGet(buff.readInt());
						buff.clear();
						latch.countDown();
					}
				}).get();
		final AtomicInteger i = new AtomicInteger(0);

		double start = System.currentTimeMillis();
		while (i.get() < runs) {
			// Operation<Buffer> op = proc.prepare();
			// op.get().append(i.getAndIncrement()).flip();
			// op.commit();
			proc.batch(512, new Consumer<Buffer>() {
				@Override
				public void accept(Buffer buff) {
					buff.append(i.getAndIncrement()).flip();
				}
			});
		}

		latch.await(5, TimeUnit.SECONDS);

		double end = System.currentTimeMillis();
		LOG.info("throughput: {}/sec", (long) (runs / ((end - start) / 1000)));

		ENV.shutdown();
	}

}
