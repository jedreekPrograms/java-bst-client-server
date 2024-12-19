package pl.bst.protocol;

import java.io.*;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import static org.junit.jupiter.api.Assertions.*;

class ProtocolTest {
    @Test void sessionsShareOnlyTheirSelectedType(){var registry=new TreeRegistry();var a=registry.open(TreeType.INTEGER);var b=registry.open(TreeType.INTEGER);
        a.execute("insert 9");assertEquals("Search 9: found",b.execute("search 9"));assertEquals("",registry.open(TreeType.STRING).execute("draw"));}
    @Test void spacesAndCommandCaseAreHandled(){var p=new TreeRegistry().open(TreeType.INTEGER);assertEquals("Inserted: 7",p.execute("  INSERT   7  "));assertEquals("7",p.execute("draw"));}
    @Test void emptyDrawAndByeHaveSingleLineResponses(){var p=new TreeRegistry().open(TreeType.INTEGER);assertEquals("",p.execute("draw"));assertEquals("Bye",p.execute("BYE"));}
    @ParameterizedTest @ValueSource(strings={""," ","insert","delete 2 3","draw extra","unknown 3","insert a"})
    void malformedIntegerCommandsReturnErrors(String command){assertTrue(new TreeRegistry().open(TreeType.INTEGER).execute(command).startsWith("ERROR"));}
    @ParameterizedTest @ValueSource(strings={"NaN","Infinity","-Infinity","1e999"})
    void nonFiniteDoublesAreRejected(String value){assertTrue(new TreeRegistry().open(TreeType.DOUBLE).execute("insert "+value).startsWith("ERROR"));}
    @Test void invalidInputDoesNotPoisonTheNextCommand(){var p=new TreeRegistry().open(TreeType.INTEGER);p.execute("insert invalid");assertEquals("Inserted: 4",p.execute("insert 4"));}
    @Test void lineFramingSupportsLFCRLFEmptyLineAndEOF()throws Exception{
        var r=new BufferedReader(new StringReader("one\r\ntwo\n\n".replace("\\r","\r").replace("\\n","\n")));
        assertEquals("one",LineProtocol.read(r,10));assertEquals("two",LineProtocol.read(r,10));assertEquals("",LineProtocol.read(r,10));assertNull(LineProtocol.read(r,10));
    }
    @Test void oversizedLineIsRejectedBeforeItsTerminator(){var r=new BufferedReader(new StringReader("12345\n"));assertThrows(IOException.class,()->LineProtocol.read(r,4));}
}
