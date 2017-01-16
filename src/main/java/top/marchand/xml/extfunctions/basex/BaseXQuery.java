/**
 * This Source Code Form is subject to the terms of 
 * the Mozilla Public License, v. 2.0. If a copy of 
 * the MPL was not distributed with this file, You 
 * can obtain one at https://mozilla.org/MPL/2.0/.
 */
package top.marchand.xml.extfunctions.basex;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.xml.transform.stream.StreamSource;
import net.sf.saxon.expr.XPathContext;
import net.sf.saxon.lib.ExtensionFunctionCall;
import net.sf.saxon.lib.ExtensionFunctionDefinition;
import net.sf.saxon.om.AxisInfo;
import net.sf.saxon.om.Item;
import net.sf.saxon.om.LazySequence;
import net.sf.saxon.om.NodeInfo;
import net.sf.saxon.om.Sequence;
import net.sf.saxon.om.SequenceIterator;
import net.sf.saxon.om.StructuredQName;
import net.sf.saxon.s9api.DocumentBuilder;
import net.sf.saxon.s9api.Processor;
import net.sf.saxon.s9api.SaxonApiException;
import net.sf.saxon.s9api.XdmNode;
import net.sf.saxon.trans.XPathException;
import net.sf.saxon.tree.iter.AxisIterator;
import net.sf.saxon.tree.tiny.TinyElementImpl;
import net.sf.saxon.value.SequenceType;
import net.sf.saxon.value.StringValue;
import org.basex.examples.api.BaseXClient;

/**
 * This class is an extension-function to Saxon.
 * It must be declared by <tt>configuration.registerExtensionFunction(new BaseXQuery());</tt>,
 * or via saxon configuration file
 * (<a href=http://www.saxonica.com/documentation9.7/index.html#!configuration/configuration-file>Saxon documentation</a>).
 * In gaulois-pipe, it just has to be in the classpath.
 * 
 * Use as :
 * <tt>declare namespace efl-ex = 'top:marchand:xml:extfunctions';
 * efl-ext:basex-query("for $i in 1 to 10 return &lt;test&gt;{$i}&lt;/test&gt;", 
 *  &lt;basex&gt;
 *      &lt;server&gt;localhost&lt;/server&gt;
 *      &lt;port&gt;1984&lt;/port&gt;
 *      &lt;user&gt;basex&lt;/user&gt;
 *      &lt;password&gt;password&lt;/password&gt;
 *  &lt;/basex&gt;
 * );</tt>
 *      
 * @author ext-cmarchand
 */
public class BaseXQuery extends ExtensionFunctionDefinition {
    public static final String EXT_NAMESPACE_URI = "top:marchand:xml:extfunctions";
    public static final String FUNCTION_NAME = "basex-query";
    public static final String EXT_NS_COMMON_PREFIX = "efl-ext";

    @Override
    public StructuredQName getFunctionQName() {
        return new StructuredQName(EXT_NS_COMMON_PREFIX, EXT_NAMESPACE_URI, FUNCTION_NAME);
    }

    @Override
    public ExtensionFunctionCall makeCallExpression() {
        return new ExtensionFunctionCall() {

            @Override
            public Sequence call(XPathContext xpc, Sequence[] sqncs) throws XPathException {
                String xquery = ((StringValue)sqncs[0].head()).getStringValue();
                TinyElementImpl basexNode = ((TinyElementImpl)sqncs[1].head());
                String server=null, port=null, user=null, password=null;
                Processor proc = new Processor(xpc.getConfiguration());
                AxisIterator iterator=basexNode.iterateAxis(AxisInfo.CHILD);
                for(NodeInfo ni = iterator.next(); ni!=null; ni=iterator.next()) {
                    switch (ni.getLocalPart()) {
                        case "server":
                            server = ni.getStringValue();
                            break;
                        case "port":
                            port = ni.getStringValue();
                            break;
                        case "user":
                            user = ni.getStringValue();
                            break;
                        case "password":
                            password = ni.getStringValue();
                            break;
                        default:
                            throw new XPathException("child elements of basex must be server, port, user and password");
                    }
                }
                try {
                    BaseXClient session = new BaseXClient(server, Integer.parseInt(port), user, password);
                    List<XdmNode> result = new ArrayList<>();
                    DocumentBuilder builder = proc.newDocumentBuilder();
                    BaseXClient.Query query = session.query(xquery);
                    BaseXSequenceIterator it = new BaseXSequenceIterator(query, builder);
                    return new LazySequence(it);
                } catch(IOException ex) {
                    throw new XPathException(ex);
                }
            }
        };
    }

    @Override
    public SequenceType[] getArgumentTypes() {
        return new SequenceType[] {SequenceType.SINGLE_STRING, SequenceType.SINGLE_ELEMENT_NODE};
    }

    @Override
    public SequenceType getResultType(net.sf.saxon.value.SequenceType[] sts) {
        return SequenceType.ANY_SEQUENCE;
    }
    
    private class BaseXSequenceIterator implements SequenceIterator, AutoCloseable {
        private final BaseXClient.Query query;
        private final DocumentBuilder builder;
        
        public BaseXSequenceIterator(BaseXClient.Query query, DocumentBuilder builder) {
            super();
            this.query=query;
            this.builder=builder;
        }

        @Override
        public Item next() throws XPathException {
            try {
                if(query.more()) {
                    StreamSource source = new StreamSource(new ByteArrayInputStream(query.next().getBytes(BaseXClient.UTF8)));
                    XdmNode node = builder.build(source);
                    return node.getUnderlyingNode();
                } else {
                    return null;
                }
            } catch(IOException | SaxonApiException ex) {
                throw new XPathException(ex);
            }
        }

        @Override
        public void close() {
            try {
                query.close();
            } catch (IOException ex) {
                Logger.getLogger(BaseXQuery.class.getName()).log(Level.SEVERE, null, ex);
            }
        }

        @Override
        public SequenceIterator getAnother() throws XPathException {
            return null;
        }

        @Override
        public int getProperties() {
            return 0;
        }
        
    }
    
}
