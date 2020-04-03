import org.junit.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.junit.Assert.*;

public class FileGraphTest {

    @Test
    public void testCase1() {
        String file = "build/resources/test/testCase1/include.cpp";

        List<String> expectedRows = Arrays.asList(
                "Line from start.cpp",
                "Line from include.cpp",
                "Line from file3.cpp"
        );
        Collections.sort(expectedRows);

        List<String> expectedInvalidLinks = Arrays.asList(
                "build/resources/test/testCase1/aaa/start.cpp::failed_1",
                "build/resources/test/testCase1/aaa/bbb/file3.cpp::failed_2"
        );
        Collections.sort(expectedInvalidLinks);

        List<String> expectedFiles = Arrays.asList(
                "build/resources/test/testCase1/aaa/start.cpp",
                "build/resources/test/testCase1/include.cpp"
        );
        Collections.sort(expectedFiles);

        FileGraph graph = new FileGraph();
        graph.addFile(file);

        ArrayList<String> resultLines = new ArrayList<>(graph.getSimpleRows());
        Collections.sort(resultLines);

        ArrayList<String> resultInvalidLinks = new ArrayList<>(graph.getInvalidLinks());
        Collections.sort(resultInvalidLinks);

        List<String> resultFiles = graph.getFilesContains("Line from start.cpp");
        Collections.sort(resultFiles);

        assertEquals(expectedRows, resultLines);
        assertEquals(expectedInvalidLinks, resultInvalidLinks);
        assertEquals(expectedFiles, resultFiles);
    }

    @Test
    public void testCase2() {
        String file = "build/resources/test/testCase2/include.cpp";

        List<String> expectedRows = Arrays.asList(
                "Line1", "Line2", "Line3"
        );
        Collections.sort(expectedRows);

        List<String> expectedFiles = Arrays.asList(
                "build/resources/test/testCase2/aaa/bbb/file3.cpp",
                "build/resources/test/testCase2/aaa/start.cpp",
                "build/resources/test/testCase2/include.cpp"
        );
        Collections.sort(expectedFiles);

        List<String> expectedInvalidLinks = Collections.singletonList(
                "build/resources/test/testCase2/include.cpp::broken("
        );

        FileGraph graph = new FileGraph();
        graph.addFile(file);

        ArrayList<String> resultLines = new ArrayList<>(graph.getSimpleRows());
        Collections.sort(resultLines);

        ArrayList<String> resultInvalidLinks = new ArrayList<>(graph.getInvalidLinks());
        Collections.sort(resultInvalidLinks);

        List<String> resultFiles = graph.getFilesContains("Line1");
        Collections.sort(resultFiles);

        assertEquals(expectedRows, resultLines);
        assertEquals(expectedInvalidLinks, resultInvalidLinks);
        assertEquals(expectedFiles, resultFiles);
    }

    @Test
    public void testEmpty() {
        FileGraph graph = new FileGraph();
        assertTrue(graph.getFilesContains("some line").isEmpty());
        assertTrue(graph.getInvalidLinks().isEmpty());
        assertTrue(graph.getSimpleRows().isEmpty());
    }

    @Test
    public void testUpdateLink() {
        String file = "build/resources/test/testCase1/include.cpp";

        List<String> expectedInvalidLinks = Arrays.asList(
                "build/resources/test/testCase1/aaa/start.cpp::failed_1",
                "build/resources/test/testCase1/aaa/bbb/file3.cpp::failed_2"
        );
        Collections.sort(expectedInvalidLinks);

        FileGraph graph = new FileGraph();
        graph.addFile(file);

        ArrayList<String> resultInvalidLinks = new ArrayList<>(graph.getInvalidLinks());
        Collections.sort(resultInvalidLinks);

        assertEquals(expectedInvalidLinks, resultInvalidLinks);
        graph.updateLink("build/resources/test/testCase1/aaa/start.cpp",
                "failed_1", "../include.cpp");
        assertEquals(1, graph.getInvalidLinks().size());
        graph.updateLink("build/resources/test/testCase1/aaa/bbb/file3.cpp",
                "failed_2", "../../include.cpp");
        assertTrue(graph.getInvalidLinks().isEmpty());
    }

    @Test
    public void testInvalidCalls() {
        FileGraph graph = new FileGraph();
        graph.updateLink("invalid path", "invalid link", "invalid too");
        assertFalse(graph.addFile("invalid").isPresent());
    }
}