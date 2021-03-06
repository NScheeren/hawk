package org.aksw.hawk.pruner;

import java.util.Set;

import org.aksw.autosparql.commons.qald.Question;
import org.aksw.hawk.querybuilding.SPARQLQuery;

import com.google.common.collect.Sets;

public class TextFilterOverVariables implements ISPARQLQueryPruner {
	private int maximalVariables = 1;

	public Set<SPARQLQuery> prune(Set<SPARQLQuery> queryStrings, Question q) {
		Set<SPARQLQuery> returnList = Sets.newHashSet();
		for (SPARQLQuery query : queryStrings) {
			if (query.textMapFromVariableToSingleFuzzyToken.size() <= maximalVariables) {
				returnList.add(query);
			}
		}
		return returnList;
	}
}
