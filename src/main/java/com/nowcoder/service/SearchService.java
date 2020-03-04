package com.nowcoder.service;

import com.nowcoder.controller.SearchController;
import com.nowcoder.model.Question;
import org.apache.http.client.HttpClient;
import org.apache.solr.client.solrj.SolrClient;
import org.apache.solr.client.solrj.SolrQuery;
import org.apache.solr.client.solrj.SolrServerException;
import org.apache.solr.client.solrj.impl.HttpSolrClient;
import org.apache.solr.client.solrj.response.QueryResponse;
import org.apache.solr.client.solrj.response.UpdateResponse;
import org.apache.solr.common.SolrDocument;
import org.apache.solr.common.SolrDocumentList;
import org.apache.solr.common.SolrInputDocument;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Created by 周杰伦 on 2018/5/15.
 */
@Service
public class SearchService {

	private static final String SOLR_URL = "http://localhost:8983/solr/zhihu";
	private SolrClient client = new HttpSolrClient.Builder(SOLR_URL).build();

	private static final Logger logger = LoggerFactory.getLogger(SearchController.class);

	/*@Autowired
	private SolrClient client;*/

	private static final String QUESTION_TITLE_FIELD = "question_title";
	private static final String QUESTION_CONTENT_FIELD = "question_content";

	public List<Question> searchQuestion(String keyword, int offset, int count,
										 String hlPre, String hlPos) throws IOException, SolrServerException {
		List<Question> questionList = new ArrayList<>();
		SolrQuery query = new SolrQuery();
		query.set("q", keyword);
		query.setRows(count);
		query.setStart(offset);
		query.set("df",QUESTION_TITLE_FIELD);
		query.setHighlight(true);
		query.setHighlightSimplePre(hlPre);
		query.setHighlightSimplePost(hlPos);
		query.set("hl.fl", QUESTION_TITLE_FIELD + "," + QUESTION_CONTENT_FIELD);
		QueryResponse response = client.query(query);
		SolrDocumentList solrDocumentList = response.getResults();
		if(solrDocumentList.isEmpty()){
			logger.error("记录数为空！！！");
		}
		for (Map.Entry<String, Map<String, List<String>>> entry : response.getHighlighting().entrySet()) {
			Question q = new Question();
			q.setId(Integer.parseInt(entry.getKey()));
			if (entry.getValue().containsKey(QUESTION_CONTENT_FIELD)) {
				List<String> contentList = entry.getValue().get(QUESTION_CONTENT_FIELD);
				if (contentList.size() > 0) {
					q.setContent(contentList.get(0));
				}
			}
			if (entry.getValue().containsKey(QUESTION_TITLE_FIELD)) {
				List<String> titleList = entry.getValue().get(QUESTION_TITLE_FIELD);
				if (titleList.size() > 0) {
					q.setTitle(titleList.get(0));
				}
			}
			questionList.add(q);
		}

		/*Map<String, Map<String, List<String>>> highlighting = response.getHighlighting();
		for (SolrDocument solrDocument : solrDocumentList) {
			//取商品信息
			Question question = new Question();
			question.setContent((String) solrDocument.get(QUESTION_CONTENT_FIELD));
			question.setId((int) solrDocument.get("id"));
			question.setTitle((String) solrDocument.get(QUESTION_TITLE_FIELD));
			questionList.add(question);
		}*/
		return questionList;
	}

	public boolean indexQuestion(int qid, String title, String content) throws Exception {
		SolrInputDocument doc = new SolrInputDocument();
		doc.setField("id", qid);
		doc.setField(QUESTION_TITLE_FIELD, title);
		doc.setField(QUESTION_CONTENT_FIELD, content);
		UpdateResponse response = client.add(doc, 1000);
		return response != null && response.getStatus() == 0;
	}
}
