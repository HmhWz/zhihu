package com.nowcoder;

import com.nowcoder.model.Question;
import org.apache.solr.client.solrj.SolrClient;
import org.apache.solr.client.solrj.SolrQuery;
import org.apache.solr.client.solrj.SolrServerException;
import org.apache.solr.client.solrj.impl.HttpSolrClient;
import org.apache.solr.client.solrj.response.QueryResponse;
import org.apache.solr.common.SolrDocument;
import org.apache.solr.common.SolrDocumentList;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class SearchTest {
	public static final String SOLR_URL = "http://localhost:8983/solr/zhihu";
	private HttpSolrClient client = new HttpSolrClient.Builder(SOLR_URL).build();

/*	@Autowired
	private SolrClient client;*/

	private static final String QUESTION_TITLE_FIELD = "question_title";
	private static final String QUESTION_CONTENT_FIELD = "question_content";

	@Test
	public void searchQuestion() throws IOException, SolrServerException {
		List<Question> questionList = new ArrayList<>();
		SolrQuery query = new SolrQuery("哈哈");
		query.setRows(0);
		query.setStart(10);
		query.setHighlight(true);
		/*query.setHighlightSimplePre(hlPre);
		query.setHighlightSimplePost(hlPos);*/
//		query.set("hl.fl", QUESTION_TITLE_FIELD + "," + QUESTION_CONTENT_FIELD);
		QueryResponse response = client.query(query);

		//取查询结果总记录数
		SolrDocumentList solrDocumentList = response.getResults();
		List<Question> itemList = new ArrayList<>();
		//取列表
		//取高亮后的结果
		Map<String, Map<String, List<String>>> highlighting = response.getHighlighting();
		for (SolrDocument solrDocument : solrDocumentList) {
			//取商品信息
			Question question = new Question();
			question.setContent((String) solrDocument.get(QUESTION_CONTENT_FIELD));
			question.setId((int) solrDocument.get("id"));
			question.setTitle((String) solrDocument.get(QUESTION_TITLE_FIELD));
			questionList.add(question);
			System.out.println(question.getTitle());
		}

	}


}
