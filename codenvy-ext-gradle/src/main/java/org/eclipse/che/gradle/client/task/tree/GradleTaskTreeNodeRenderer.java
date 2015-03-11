/*******************************************************************************
 * Copyright (c) 2012-2015 Codenvy, S.A.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *   Codenvy, S.A. - initial API and implementation
 *******************************************************************************/
package org.eclipse.che.gradle.client.task.tree;

import elemental.dom.Element;
import elemental.html.SpanElement;

import com.google.gwt.resources.client.CssResource;
import com.google.gwt.user.client.ui.UIObject;
import com.google.inject.Inject;

import org.eclipse.che.gradle.dto.GrdTask;
import org.eclipse.che.ide.api.project.tree.TreeNode;
import org.eclipse.che.ide.ui.Tooltip;
import org.eclipse.che.ide.ui.menu.PositionController;
import org.eclipse.che.ide.ui.tree.NodeRenderer;
import org.eclipse.che.ide.ui.tree.Tree;
import org.eclipse.che.ide.ui.tree.TreeNodeElement;
import org.eclipse.che.ide.util.TextUtils;
import org.eclipse.che.ide.util.dom.Elements;
import org.vectomatic.dom.svg.ui.SVGImage;
import org.vectomatic.dom.svg.ui.SVGResource;

/** @author Vladyslav Zhukovskii */
public class GradleTaskTreeNodeRenderer implements NodeRenderer<TreeNode<?>> {
    private final TaskResources.Css css;
    private final TaskResources     taskResources;

    @Inject
    public GradleTaskTreeNodeRenderer(TaskResources taskResources) {
        this.taskResources = taskResources;
        this.css = taskResources.gradleTaskTreeNodeRendererCss();
        css.ensureInjected();
    }

    @Override
    public Element getNodeKeyTextContainer(SpanElement treeNodeLabel) {
        return (Element)treeNodeLabel.getChildNodes().item(1);
    }

    @Override
    public SpanElement renderNodeContents(TreeNode<?> data) {
        SpanElement root = Elements.createSpanElement(css.root(), css.label());

        SpanElement nodeText = Elements.createSpanElement(css.taskFont());

        SVGImage icon;
        if (data instanceof GradleTaskNode) {
            GrdTask task = ((GradleTaskNode)data).getData();

            if (task.getDescription() != null) {
                Tooltip.create(nodeText,
                               PositionController.VerticalAlign.MIDDLE,
                               PositionController.HorizontalAlign.LEFT,
                               task.getDescription());
            }

            SpanElement iconWrapper = Elements.createSpanElement();
            icon = new SVGImage(((GradleTaskNode)data).isEnabled() ? taskResources.enabled() : taskResources.disabled());
            icon.getElement().setAttribute("class", css.icon());
            iconWrapper.appendChild((Element)icon.getElement());

            Tooltip.create(iconWrapper,
                           PositionController.VerticalAlign.MIDDLE,
                           PositionController.HorizontalAlign.LEFT,
                           ((GradleTaskNode)data).isEnabled() ? "Enabled" : "Disabled");

            root.appendChild(iconWrapper);
        } else {
            icon = new SVGImage(taskResources.gradle());
            icon.getElement().setAttribute("class", css.icon());
            root.appendChild((Element)icon.getElement());
            Elements.addClassName(css.moduleFont(), root);
        }

        nodeText.setInnerHTML("&nbsp;" + data.getDisplayName());

        root.appendChild(nodeText);

        // set 'id' property for rendered element (it's need for testing purpose)
        setIdProperty((com.google.gwt.dom.client.Element)root, data);

        return root;
    }

    private void setIdProperty(com.google.gwt.dom.client.Element element, TreeNode<?> node) {
        String id = "/" + node.getId();
        TreeNode<?> parent = node.getParent();
        while (parent != null && !parent.getId().equals("ROOT")) {
            id = "/" + parent.getId() + id;
            parent = parent.getParent();
        }
        UIObject.ensureDebugId(element, "taskTree-" + TextUtils.md5(id));
    }

    public interface TaskResources extends Tree.Resources {
        public interface Css extends CssResource {
            String icon();

            String label();

            String moduleFont();

            String taskFont();

            String root();
        }

        @Source({"task.css", "com/codenvy/ide/common/constants.css", "com/codenvy/ide/api/ui/style.css"})
        Css gradleTaskTreeNodeRendererCss();

        @Source("task.svg")
        SVGResource task();

        @Source("gradle.svg")
        SVGResource gradle();

        @Source("enabled.svg")
        SVGResource enabled();

        @Source("disabled.svg")
        SVGResource disabled();
    }


    @Override
    public void updateNodeContents(TreeNodeElement<TreeNode<?>> treeNode) {
    }
}
