package at.favre.lib.crypto.bcrypt.benchmark;

import at.favre.lib.bytes.BinaryToTextEncoding;
import at.favre.lib.bytes.Bytes;
import at.favre.lib.crypto.bcrypt.BCrypt;
import com.github.fzakaria.ascii85.Ascii85;
import org.openjdk.jmh.annotations.*;

import java.nio.ByteOrder;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.TimeUnit;

@SuppressWarnings("CheckStyle")
@State(Scope.Thread)
@Fork(1)
@Warmup(iterations = 2, time = 4)
@Measurement(iterations = 3, time = 12)
@BenchmarkMode(Mode.AverageTime)
@OutputTimeUnit(TimeUnit.MILLISECONDS)
//CHECKSTYLE.OFF
public class BcryptBenchmark {

    private AbstractBcrypt favreBcrypt;
    private AbstractBcrypt jBcrypt;
    private AbstractBcrypt bcBcrypt;
    private BinaryToTextEncoding.Encoder ascii85 = new BinaryToTextEncoding.Encoder() {
        @Override
        public String encode(byte[] bytes, ByteOrder byteOrder) {
            return Ascii85.encode(bytes);
        }
    };

    @Param({"10", "12"})
    private int cost;
    private byte[] pw;

    @Setup(Level.Trial)
    public void setupBenchmark() {
        favreBcrypt = new FavreBcrypt();
        jBcrypt = new JBcrypt();
        bcBcrypt = new BC();
    }

    @Setup(Level.Invocation)
    public void setup() {
        pw = Bytes.random(36).encode(ascii85).getBytes(StandardCharsets.UTF_8);
    }

    @Benchmark
    public byte[] benchmarkFavreBcrypt() {
        return benchmark(favreBcrypt, cost, pw);
    }

    //@Benchmark
    public byte[] benchmarkJBcrypt() {
        return benchmark(jBcrypt, cost, pw);
    }

    //@Benchmark
    public byte[] benchmarkBcBcrypt() {
        return benchmark(bcBcrypt, cost, pw);
    }

    private byte[] benchmark(AbstractBcrypt bcrypt, int logRounds, byte[] pw) {
        return bcrypt.bcrypt(logRounds, pw);
    }

    static final class FavreBcrypt implements AbstractBcrypt {
        @Override
        public byte[] bcrypt(int cost, byte[] password) {
            return BCrypt.withDefaults().hash(cost, password);
        }
    }

    static final class JBcrypt implements AbstractBcrypt {
        @Override
        public byte[] bcrypt(int cost, byte[] password) {
            return org.mindrot.jbcrypt.BCrypt.hashpw(new String(password, StandardCharsets.UTF_8), org.mindrot.jbcrypt.BCrypt.gensalt(cost)).getBytes(StandardCharsets.UTF_8);
        }
    }

    static final class BC implements AbstractBcrypt {
        @Override
        public byte[] bcrypt(int cost, byte[] password) {
            return org.bouncycastle.crypto.generators.BCrypt.generate(Bytes.from(password).append((byte) 0).array(), Bytes.random(16).array(), cost);
        }
    }

    interface AbstractBcrypt {
        byte[] bcrypt(int cost, byte[] password);
    }
}
//CHECKSTYLE.ON
