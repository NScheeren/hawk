package org.aksw.hawk.experiment;

import java.io.IOException;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.xml.parsers.ParserConfigurationException;

import org.aksw.autosparql.commons.qald.Question;
import org.aksw.hawk.controller.Answer;
import org.aksw.hawk.controller.Pipeline;
import org.aksw.hawk.ranking.BucketRanker;
import org.aksw.hawk.ranking.FeatureBasedRanker;
import org.aksw.hawk.ranking.FeatureBasedRanker.Feature;
import org.aksw.hawk.ranking.OptimalRanker;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Joiner;
import com.google.common.collect.Sets;

/**
 * Made for testing a single pipeline
 * 
 * @author Lorenz Buehmann
 * @author ricardousbeck
 * 
 */
public class SingleQuestionPipeline {
	static Logger log = LoggerFactory.getLogger(SingleQuestionPipeline.class);

	public static void main(String args[]) throws IOException, ParserConfigurationException {
		log.info("Configuring controller");
		Pipeline pipeline = new Pipeline();

		Question q = new Question();
		q.languageToQuestion.put("en", "In which city was the assassin of Martin Luther King born?");

		log.info("Run pipeline on " + q.languageToQuestion.get("en"));
		List<Answer> answers = pipeline.getAnswersToQuestion(q);

		// ##############~~RANKING~~##############
		log.info("Run ranking");
		//FIXME optimal ranking can only determined if question matches one of the QALD questions
		int maximumPositionToMeasure = 10;
		OptimalRanker optimal_ranker = new OptimalRanker();
		FeatureBasedRanker feature_ranker = new FeatureBasedRanker();
		BucketRanker bucket_ranker = new BucketRanker();

		// optimal ranking
		log.info("Optimal ranking not applicable (right now).");
		// List<Set<RDFNode>> rankedAnswer = optimal_ranker.rank(answers, q);
		// List<EvalObj> eval = Measures.measure(rankedAnswer, q, maximumPositionToMeasure);
		// log.info(Joiner.on("\n\t").join(eval));

		// feature-based ranking
		log.info("Feature-based ranking begins training.");
		for (Set<Feature> featureSet : Sets.powerSet(new HashSet<>(Arrays.asList(Feature.values())))) {
			if (!featureSet.isEmpty()) {
				log.debug("Feature-based ranking: " + featureSet.toString());
				feature_ranker.setFeatures(featureSet);
				feature_ranker.train();
				List<Answer> rankedAnswer = feature_ranker.rank(answers, q);
				log.info(Joiner.on("\n\t").join(rankedAnswer));
			}
		}

		// bucket-based ranking
		log.info("Bucket-based ranking");
		List<Answer> rankedAnswer = bucket_ranker.rank(answers, q);
		log.info(Joiner.on("\n\t").join(rankedAnswer));

	}

}
