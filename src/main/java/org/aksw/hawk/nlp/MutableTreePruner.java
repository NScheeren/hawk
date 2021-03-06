package org.aksw.hawk.nlp;

import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Queue;

import org.aksw.autosparql.commons.qald.Question;
import org.aksw.hawk.util.JSONStatusBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.collect.Lists;
import com.google.common.collect.Queues;

public class MutableTreePruner {
	Logger log = LoggerFactory.getLogger(MutableTreePruner.class);

	public MutableTree prune(Question q) {
		log.debug(q.tree.toString());
		removalRules(q);
		removalBasedOnDependencyLabels(q);
		/*
		 * interrogative rules last else each interrogative word has at least two children, which can't be handled yet by the removal
		 */
		applyInterrogativeRules(q);
		sortTree(q.tree);
		log.debug(q.tree.toString());
		q.tree_pruned = JSONStatusBuilder.treeToJSON(q.tree);
		return q.tree;
	}

	private void sortTree(MutableTree tree) {
		Queue<MutableTreeNode> queue = new LinkedList<MutableTreeNode>();
		queue.add(tree.getRoot());
		while (!queue.isEmpty()) {
			MutableTreeNode tmp = queue.poll();
			Collections.sort(tmp.getChildren());
			queue.addAll(tmp.getChildren());
		}

	}

	private void removalBasedOnDependencyLabels(Question q) {
		for (String depLabel : Lists.newArrayList("auxpass", "aux")) {
			inorderRemovalBasedOnDependencyLabels(q.tree.getRoot(), q.tree, depLabel);
		}
	}

	private boolean inorderRemovalBasedOnDependencyLabels(MutableTreeNode node, MutableTree tree, String depLabel) {
		if (node.depLabel.matches(depLabel)) {
			tree.remove(node);
			return true;
		} else {
			for (Iterator<MutableTreeNode> it = node.getChildren().iterator(); it.hasNext();) {
				MutableTreeNode child = it.next();
				if (inorderRemovalBasedOnDependencyLabels(child, tree, depLabel)) {
					it = node.getChildren().iterator();
				}
			}
			return false;
		}
	}

	private void applyInterrogativeRules(Question q) {
		MutableTreeNode root = q.tree.getRoot();
		// GIVE ME will be deleted
		if (root.label.equals("Give")) {
			for (Iterator<MutableTreeNode> it = root.getChildren().iterator(); it.hasNext();) {
				MutableTreeNode next = it.next();
				if (next.label.equals("me")) {
					it.remove();
					q.tree.remove(root);
				}
			}
		}
		// LIST will be deleted
		if (root.label.equals("List")) {
			q.tree.remove(root);
		}
		// GIVE will be deleted
		if (root.label.equals("Give")) {
			q.tree.remove(root);
		}

	}

	/**
	 * removes: * punctuations (.) * wh- words(WDT|WP$) * PRP($) * DT * BY and IN (possessive) pronouns * PDT predeterminer all both
	 * 
	 * Who,Where WP|WRB stays in
	 * 
	 * @param q
	 */
	private void removalRules(Question q) {
		MutableTreeNode root = q.tree.getRoot();
		for (String posTag : Lists.newArrayList(".", "WDT", "POS", "WP\\$", "PRP\\$", "RB", "PRP", "DT", "IN", "PDT")) {
			Queue<MutableTreeNode> queue = Queues.newLinkedBlockingQueue();
			queue.add(root);
			while (!queue.isEmpty()) {
				MutableTreeNode tmp = queue.poll();
				if (tmp.posTag.matches(posTag)) {
					q.tree.remove(tmp);
				}
				for (MutableTreeNode n : tmp.getChildren()) {
					queue.add(n);
				}
			}
		}

	}

}
