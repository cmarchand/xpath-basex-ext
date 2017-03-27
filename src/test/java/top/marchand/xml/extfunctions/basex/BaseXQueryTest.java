/**
 * This Source Code Form is subject to the terms of 
 * the Mozilla Public License, v. 2.0. If a copy of 
 * the MPL was not distributed with this file, You 
 * can obtain one at https://mozilla.org/MPL/2.0/.
 */
package top.marchand.xml.extfunctions.basex;

import net.sf.saxon.Configuration;
import net.sf.saxon.om.StructuredQName;
import net.sf.saxon.s9api.DocumentBuilder;
import net.sf.saxon.s9api.Processor;
import net.sf.saxon.s9api.QName;
import net.sf.saxon.s9api.XPathCompiler;
import net.sf.saxon.s9api.XPathSelector;
import net.sf.saxon.s9api.XdmNode;
import net.sf.saxon.value.SequenceType;
import org.basex.BaseXServer;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import javax.xml.transform.stream.StreamSource;
import net.sf.saxon.om.Item;
import net.sf.saxon.om.SequenceIterator;
import net.sf.saxon.s9api.Axis;
import net.sf.saxon.s9api.SaxonApiException;
import net.sf.saxon.s9api.XdmValue;
import net.sf.saxon.s9api.XsltCompiler;
import net.sf.saxon.s9api.XsltTransformer;
import net.sf.saxon.trans.XPathException;
//import org.apache.commons.io.output.NullOutputStream;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 *
 * @author ext-cmarchand
 */
public class BaseXQueryTest {
    private static final String CONNECT_STRING =
            "<?xml version='1.0' encoding='UTF-8'?>"+
            "<basex>"+
                "<server>localhost</server>"+
                "<port>1984</port>"+
                "<user>admin</user>"+
                "<password>admin</password>"+
            "</basex>";
    private static BaseXServer server;
    
    @BeforeClass
    public static void initServer() throws Exception {
        server = new BaseXServer();
    }
    
    @AfterClass
    public static void stopServer() throws Exception {
        if(server!=null) {
            try {
                server.stop();
                server = null;
            } catch(Exception ex) {
                // ignore
            }
        }
    }

    /**
     * Test of getFunctionQName method, of class BaseXQuery.
     */
    @Test
    public void testGetFunctionQName() {
        BaseXQuery instance = new BaseXQuery();
        StructuredQName expResult = new StructuredQName("efl-ext", "top:marchand:xml:extfunctions", "basex-query");
        StructuredQName result = instance.getFunctionQName();
        assertEquals(expResult, result);
    }

    /**
     * Test of makeCallExpression method, of class BaseXQuery.
     */
    @Test
    public void testMakeCallExpression2args() {
        Configuration config = new Configuration();
        config.registerExtensionFunction(new BaseXQuery());
        Processor proc = new Processor(config);
        XPathCompiler xpc = proc.newXPathCompiler();
        try {
            xpc.declareNamespace(BaseXQuery.EXT_NS_COMMON_PREFIX, BaseXQuery.EXT_NAMESPACE_URI);
            QName var = new QName("connect");
            xpc.declareVariable(var);
            XPathSelector xp = xpc.compile(BaseXQuery.EXT_NS_COMMON_PREFIX+":"+BaseXQuery.FUNCTION_NAME+"('for $i in 1 to 10 return <test>{$i}</test>', $connect)").load();
            DocumentBuilder builder = proc.newDocumentBuilder();
            XdmNode docConnect = builder.build(new StreamSource(new ByteArrayInputStream(CONNECT_STRING.getBytes("UTF-8"))));
            XdmNode connect = (XdmNode)docConnect.axisIterator(Axis.DESCENDANT_OR_SELF, new QName("basex")).next();
            
            xp.setVariable(var, connect);
            xp.setContextItem(docConnect);
            XdmValue result = xp.evaluate();
            SequenceIterator it = result.getUnderlyingValue().iterate();
            Item item = it.next();
            int count = 1;
            while(item!=null) {
                assertEquals(Integer.toString(count++),item.getStringValue());
                item=it.next();
            }
            it.close();
        } catch(SaxonApiException | UnsupportedEncodingException | XPathException ex) {
            ex.printStackTrace(System.err);
            fail(ex.getMessage());
        }
    }
    @Test
    public void testMakeCallExpression5args() {
        Configuration config = new Configuration();
        config.registerExtensionFunction(new BaseXQuery());
        Processor proc = new Processor(config);
        XPathCompiler xpc = proc.newXPathCompiler();
        try {
            xpc.declareNamespace(BaseXQuery.EXT_NS_COMMON_PREFIX, BaseXQuery.EXT_NAMESPACE_URI);
            XPathSelector xp = xpc.compile(BaseXQuery.EXT_NS_COMMON_PREFIX+":"+BaseXQuery.FUNCTION_NAME+"('for $i in 1 to 10 return <test>{$i}</test>', 'localhost', '1984', 'admin', 'admin')").load();

            DocumentBuilder builder = proc.newDocumentBuilder();
            XdmNode docConnect = builder.build(new StreamSource(new ByteArrayInputStream("<document />".getBytes("UTF-8"))));

            xp.setContextItem(docConnect);
            XdmValue result = xp.evaluate();
            SequenceIterator it = result.getUnderlyingValue().iterate();
            Item item = it.next();
            int count = 1;
            while(item!=null) {
                assertEquals(Integer.toString(count++),item.getStringValue());
                item=it.next();
            }
            it.close();
        } catch(SaxonApiException | UnsupportedEncodingException | XPathException ex) {
            ex.printStackTrace(System.err);
            fail(ex.getMessage()); 
        }
    }
    @Test(expected = SaxonApiException.class)
    public void testMakeCallExpression3args() throws SaxonApiException, XPathException {
        Configuration config = new Configuration();
        config.registerExtensionFunction(new BaseXQuery());
        Processor proc = new Processor(config);
        XPathCompiler xpc = proc.newXPathCompiler();
        try {
            xpc.declareNamespace(BaseXQuery.EXT_NS_COMMON_PREFIX, BaseXQuery.EXT_NAMESPACE_URI);
            XPathSelector xp = xpc.compile(
                    BaseXQuery.EXT_NS_COMMON_PREFIX+":"+BaseXQuery.FUNCTION_NAME+"('for $i in 1 to 10 return <test>{$i}</test>', 'localhost', '1984')").load();
            DocumentBuilder builder = proc.newDocumentBuilder();
            XdmNode docConnect = builder.build(new StreamSource(new ByteArrayInputStream("<document />".getBytes("UTF-8"))));
            xp.setContextItem(docConnect);
            XdmValue result = xp.evaluate();
            SequenceIterator it = result.getUnderlyingValue().iterate();
            Item item = it.next();
            int count = 1;
            while(item!=null) {
                assertEquals(Integer.toString(count++),item.getStringValue());
                item=it.next();
            }
            it.close();
        } catch(SaxonApiException | XPathException ex) {
            throw ex;
        } catch(UnsupportedEncodingException ex) {
            // do nothing, it won't happen
        }
    }
    @Test(expected = SaxonApiException.class)
    public void testMakeCallExpression4args() throws SaxonApiException, XPathException {
        Configuration config = new Configuration();
        config.registerExtensionFunction(new BaseXQuery());
        Processor proc = new Processor(config);
        XPathCompiler xpc = proc.newXPathCompiler();
        try {
            xpc.declareNamespace(BaseXQuery.EXT_NS_COMMON_PREFIX, BaseXQuery.EXT_NAMESPACE_URI);
            XPathSelector xp = xpc.compile(
                    BaseXQuery.EXT_NS_COMMON_PREFIX+":"+BaseXQuery.FUNCTION_NAME+"('for $i in 1 to 10 return <test>{$i}</test>', 'localhost', '1984')").load();
            DocumentBuilder builder = proc.newDocumentBuilder();
            XdmNode docConnect = builder.build(new StreamSource(new ByteArrayInputStream("<document />".getBytes("UTF-8"))));
            xp.setContextItem(docConnect);
            XdmValue result = xp.evaluate();
            SequenceIterator it = result.getUnderlyingValue().iterate();
            Item item = it.next();
            int count = 1;
            while(item!=null) {
                assertEquals(Integer.toString(count++),item.getStringValue());
                item=it.next();
            }
            it.close();
        } catch(SaxonApiException | XPathException ex) {
            throw ex;
        } catch(UnsupportedEncodingException ex) {
            // do nothing, it won't happen
        }
    }
    @Test(expected = SaxonApiException.class)
    public void testMakeCallExpression2argsWrongType() throws SaxonApiException, XPathException {
        Configuration config = new Configuration();
        config.registerExtensionFunction(new BaseXQuery());
        Processor proc = new Processor(config);
        XPathCompiler xpc = proc.newXPathCompiler();
        try {
            xpc.declareNamespace(BaseXQuery.EXT_NS_COMMON_PREFIX, BaseXQuery.EXT_NAMESPACE_URI);
            QName var = new QName("connect");
            xpc.declareVariable(var);
            XPathSelector xp = xpc.compile(BaseXQuery.EXT_NS_COMMON_PREFIX+":"+BaseXQuery.FUNCTION_NAME+"('for $i in 1 to 10 return <test>{$i}</test>', 'connect')").load();
            DocumentBuilder builder = proc.newDocumentBuilder();
            XdmNode docConnect = builder.build(new StreamSource(new ByteArrayInputStream(CONNECT_STRING.getBytes("UTF-8"))));
            XdmNode connect = (XdmNode)docConnect.axisIterator(Axis.DESCENDANT_OR_SELF, new QName("basex")).next();
            
            xp.setVariable(var, connect);
            xp.setContextItem(docConnect);
            XdmValue result = xp.evaluate();
            SequenceIterator it = result.getUnderlyingValue().iterate();
            Item item = it.next();
            int count = 1;
            while(item!=null) {
                assertEquals(Integer.toString(count++),item.getStringValue());
                item=it.next();
            }
            it.close();
        } catch(SaxonApiException | XPathException ex) {
            throw ex;
        } catch(UnsupportedEncodingException ex) {
            // do nothing, it won't happen
        }
    }
    @Test(expected = SaxonApiException.class)
    public void testMakeCallExpression5argsWrongType() throws SaxonApiException, XPathException {
        Configuration config = new Configuration();
        config.registerExtensionFunction(new BaseXQuery());
        Processor proc = new Processor(config);
        XPathCompiler xpc = proc.newXPathCompiler();
        try {
            xpc.declareNamespace(BaseXQuery.EXT_NS_COMMON_PREFIX, BaseXQuery.EXT_NAMESPACE_URI);
            QName var = new QName("connect");
            xpc.declareVariable(var);
            XPathSelector xp = xpc.compile(BaseXQuery.EXT_NS_COMMON_PREFIX+":"+BaseXQuery.FUNCTION_NAME+"('for $i in 1 to 10 return <test>{$i}</test>', $connect, '1984', 'admin', 'admin')").load();
            DocumentBuilder builder = proc.newDocumentBuilder();
            XdmNode docConnect = builder.build(new StreamSource(new ByteArrayInputStream(CONNECT_STRING.getBytes("UTF-8"))));
            XdmNode connect = (XdmNode)docConnect.axisIterator(Axis.DESCENDANT_OR_SELF, new QName("basex")).next();
            
            xp.setVariable(var, connect);
            xp.setContextItem(docConnect);
            XdmValue result = xp.evaluate();
            SequenceIterator it = result.getUnderlyingValue().iterate();
            Item item = it.next();
            int count = 1;
            while(item!=null) {
                assertEquals(Integer.toString(count++),item.getStringValue());
                item=it.next();
            }
            it.close();
        } catch(SaxonApiException | XPathException ex) {
            throw ex;
        } catch(UnsupportedEncodingException ex) {
            // do nothing, it won't happen
        }
    }

    /**
     * Test of getArgumentTypes method, of class BaseXQuery.
     */
    @Test
    public void testGetArgumentTypes() {
        BaseXQuery instance = new BaseXQuery();
        SequenceType[] expResult = new SequenceType[] {SequenceType.SINGLE_STRING, SequenceType.SINGLE_ITEM, SequenceType.OPTIONAL_STRING, SequenceType.OPTIONAL_STRING, SequenceType.OPTIONAL_STRING};
        SequenceType[] result = instance.getArgumentTypes();
        assertEquals(expResult.length, result.length);
        for(int i=0;i<expResult.length;i++)
            assertEquals("entry "+i+" differs from expected",expResult[i], result[i]);
    }

    /**
     * Test of getResultType method, of class BaseXQuery.
     */
    @Test
    public void testGetResultType() {
        SequenceType[] sts = null;
        BaseXQuery instance = new BaseXQuery();
        SequenceType expResult = SequenceType.ANY_SEQUENCE;
        SequenceType result = instance.getResultType(sts);
        assertEquals(expResult, result);
    }
    @Test
    public void testXsl1() {
        Configuration config = new Configuration();
        config.registerExtensionFunction(new BaseXQuery());
        Processor proc = new Processor(config);
        XsltCompiler compiler = proc.newXsltCompiler();
        try {
            InputStream is = new FileInputStream(new File(new File(System.getProperty("user.dir")),"src/test/resources/test1.xsl"));
            XsltTransformer t = compiler.compile(new StreamSource(is)).load();
            t.setDestination(proc.newSerializer(new File("target/generated-test-files/result1.xml")));
            t.setInitialContextNode(proc.newDocumentBuilder().build(new StreamSource(new FileInputStream(new File(new File(System.getProperty("user.dir")),"src/test/resources/test1.xsl")))));
            t.transform();
        } catch (SaxonApiException | FileNotFoundException ex) {
            ex.printStackTrace(System.err);
            fail(ex.getMessage());
        }
    }
    @Test
    public void testXsl2() {
        Configuration config = new Configuration();
        config.registerExtensionFunction(new BaseXQuery());
        Processor proc = new Processor(config);
        XsltCompiler compiler = proc.newXsltCompiler();
        try {
            InputStream is = new FileInputStream(new File(new File(System.getProperty("user.dir")),"src/test/resources/test2.xsl"));
            XsltTransformer t = compiler.compile(new StreamSource(is)).load();
            t.setDestination(proc.newSerializer(new File("target/generated-test-files/result2.xml")));
            t.setInitialContextNode(proc.newDocumentBuilder().build(new StreamSource(new FileInputStream(new File(new File(System.getProperty("user.dir")),"src/test/resources/test2.xsl")))));
            t.transform();
        } catch (SaxonApiException | FileNotFoundException ex) {
            ex.printStackTrace(System.err);
            fail(ex.getMessage());
        }
    }
    @Test
    public void testXsl3() {
        Configuration config = new Configuration();
        config.registerExtensionFunction(new BaseXQuery());
        Processor proc = new Processor(config);
        XsltCompiler compiler = proc.newXsltCompiler();
        try {
            InputStream is = new FileInputStream(new File(new File(System.getProperty("user.dir")),"src/test/resources/test3.xsl"));
            XsltTransformer t = compiler.compile(new StreamSource(is)).load();
            t.setDestination(proc.newSerializer(new File("target/generated-test-files/result3.xml")));
            t.setInitialContextNode(proc.newDocumentBuilder().build(new StreamSource(new FileInputStream(new File(new File(System.getProperty("user.dir")),"src/test/resources/test3.xsl")))));
            t.transform();
        } catch (SaxonApiException | FileNotFoundException ex) {
            ex.printStackTrace(System.err);
            fail(ex.getMessage());
        }
    }
}
