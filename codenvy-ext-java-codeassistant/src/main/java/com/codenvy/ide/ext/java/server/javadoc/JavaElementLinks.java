package com.codenvy.ide.ext.java.server.javadoc;

import org.eclipse.jdt.core.IJavaElement;

import java.net.URI;
import java.net.URISyntaxException;

/**
 * @author Evgen Vidolob
 */
public class JavaElementLinks {

    public static final String JAVADOC_SCHEME= "eclipse-javadoc";

    private static final char LINK_BRACKET_REPLACEMENT= '\u2603';
    /**
     * The link is composed of a number of segments, separated by LINK_SEPARATOR:
     * <p>
     * segments[0]: ""<br>
     * segments[1]: baseElementHandle<br>
     * segments[2]: typeName<br>
     * segments[3]: memberName<br>
     * segments[4...]: parameterTypeName (optional)
     */
    private static final char LINK_SEPARATOR= '\u2602';

    /**
     * Creates an {@link java.net.URI} with the given scheme for the given element.
     *
     * @param scheme the scheme
     * @param element the element
     * @return an {@link java.net.URI}, encoded as {@link java.net.URI#toASCIIString() ASCII} string, ready to be used
     *         as <code>href</code> attribute in an <code>&lt;a&gt;</code> tag
     * @throws java.net.URISyntaxException if the arguments were invalid
     */
    public static String createURI(String scheme, IJavaElement element) throws URISyntaxException {
        return createURI(scheme, element, null, null, null);
    }

    /**
     * Creates an {@link java.net.URI} with the given scheme based on the given element.
     * The additional arguments specify a member referenced from the given element.
     *
     * @param scheme a scheme
     * @param element the declaring element
     * @param refTypeName a (possibly qualified) type or package name, can be <code>null</code>
     * @param refMemberName a member name, can be <code>null</code>
     * @param refParameterTypes a (possibly empty) array of (possibly qualified) parameter type
     *            names, can be <code>null</code>
     * @return an {@link java.net.URI}, encoded as {@link java.net.URI#toASCIIString() ASCII} string, ready to be used
     *         as <code>href</code> attribute in an <code>&lt;a&gt;</code> tag
     * @throws URISyntaxException if the arguments were invalid
     */
    public static String createURI(String scheme, IJavaElement element, String refTypeName, String refMemberName, String[] refParameterTypes) throws URISyntaxException {
		/*
		 * We use an opaque URI, not ssp and fragments (to work around Safari bug https://bugs.eclipse.org/bugs/show_bug.cgi?id=212527 (wrongly encodes #)).
		 */

        StringBuffer ssp= new StringBuffer(60);
        ssp.append(LINK_SEPARATOR); // make sure first character is not a / (would be hierarchical URI)

        // replace '[' manually, since URI confuses it for an IPv6 address as per RFC 2732:
        ssp.append(element.getHandleIdentifier().replace('[', LINK_BRACKET_REPLACEMENT)); // segments[1]

        if (refTypeName != null) {
            ssp.append(LINK_SEPARATOR);
            ssp.append(refTypeName); // segments[2]

            if (refMemberName != null) {
                ssp.append(LINK_SEPARATOR);
                ssp.append(refMemberName); // segments[3]

                if (refParameterTypes != null) {
                    ssp.append(LINK_SEPARATOR);
                    for (int i= 0; i < refParameterTypes.length; i++) {
                        ssp.append(refParameterTypes[i]); // segments[4|5|..]
                        if (i != refParameterTypes.length - 1) {
                            ssp.append(LINK_SEPARATOR);
                        }
                    }
                }
            }
        }
        return new URI(scheme, ssp.toString(), null).toASCIIString();
    }

    /**
     * Creates a link with the given URI and label text.
     *
     * @param uri the URI
     * @param label the label
     * @return the HTML link
     * @since 3.6
     */
    public static String createLink(String uri, String label) {
        return "<a href='" + uri + "'>" + label + "</a>"; //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
    }
}
