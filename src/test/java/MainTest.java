import org.junit.Test;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.PrintStream;

import static org.junit.Assert.assertEquals;

public class MainTest {

    @Test
    public void serializationTest() {
        String inputString1 =
                ":add build/resources/test/testCase1/include.cpp\n"
                        + ":save dump";
        String inputString2 =
                ":lines\n"
                        + ":load dump\n"
                        + ":lines";

        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        System.setOut(new PrintStream(outputStream));

        System.setIn(new ByteArrayInputStream(inputString1.getBytes()));
        Main.main(new String[]{});
        System.setIn(new ByteArrayInputStream(inputString2.getBytes()));
        Main.main(new String[]{});

        String expected = "Known lines:\n" +
                "Known lines:\n" +
                "Line from include.cpp\n" +
                "Line from start.cpp\n" +
                "Line from file3.cpp";

        assertEquals(expected.trim(), outputStream.toString().trim());
    }

}