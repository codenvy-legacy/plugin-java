/*******************************************************************************
 * Copyright (c) 2012-2014 Codenvy, S.A.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Codenvy, S.A. - initial API and implementation
 *******************************************************************************/

package com.codenvy.ide.ext.java.messages;

import com.google.gwt.webworker.client.messages.MessageImpl;

/**
 * @author Evgen Vidolob
 */
public class JavadocHandleComputed extends MessageImpl {
    protected JavadocHandleComputed() {
    }

    public static native JavadocHandleComputed make() /*-{
        return {
            _type : 16
        }
    }-*/;

    public final native String getHandle() /*-{
        return this["handle"];
    }-*/;

    public final native JavadocHandleComputed setHandle(String handle) /*-{
        this["handle"] = handle;
        return this;
    }-*/;

    public final native java.lang.String getId() /*-{
        return this["id"];
    }-*/;

    public final native JavadocHandleComputed setId(java.lang.String id) /*-{
        this["id"] = id;
        return this;
    }-*/;

    public final native boolean hasId() /*-{
        return this.hasOwnProperty("id");
    }-*/;
}
