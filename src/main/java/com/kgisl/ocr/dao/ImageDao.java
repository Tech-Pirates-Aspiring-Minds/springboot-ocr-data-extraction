package com.kgisl.ocr.dao;

import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.hibernate.Criteria;
import org.hibernate.SQLQuery;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.cfg.Configuration;
import org.hibernate.query.Query;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

import com.fasterxml.jackson.databind.ObjectMapper;

@SuppressWarnings("deprecation")
public class ImageDao {

	private static SessionFactory factory;

	public List<?> dataExtractionOutput(Integer ocrId) {
		try {
			factory = new Configuration().configure().buildSessionFactory();
		} catch (Throwable ex) {
			System.err.println("Failed to create sessionFactory object." + ex);
			throw new ExceptionInInitializerError(ex);
		}
		Session session = factory.openSession();

		String sql = "SELECT ocr.ocr_template_Name,map.ocr_field_name,map.ocr_field_coordinates FROM ocr_template_mapping ocr JOIN ocr_coordinates_mapping map ON map.ocr_template_id=ocr.ocr_id WHERE ocr.ocr_id=1";
		SQLQuery<?> query = session.createSQLQuery(sql);
		query.setResultTransformer(Criteria.ALIAS_TO_ENTITY_MAP);
		List<?> results = query.list();
		// closing the session
		session.close();

		return results;
	}

	public void saveOcrCoordinates(String templateName, Object obj) throws Exception {
		factory = new Configuration().configure().buildSessionFactory();

		Session session = factory.openSession();
		try {

			session.beginTransaction();
			ObjectMapper oMapper = new ObjectMapper();
			Authentication auth = SecurityContextHolder.getContext().getAuthentication();
			StringBuilder sql = new StringBuilder(); // Using default 16 character size

			sql.append("INSERT INTO ocr_template_mapping (ocr_template_Name,ocr_created_user) VALUES('" + templateName
					+ "','" + auth.getName() + "');");
			sql.append("SELECT LAST_INSERT_ID() INTO @templateId;");
			@SuppressWarnings("unchecked")
			Map<String, Object> map = oMapper.convertValue(obj, Map.class);
			for (Entry<String, Object> entrys : map.entrySet()) {
				sql.append(
						"INSERT INTO ocr_coordinates_mapping (ocr_template_id,ocr_field_name,ocr_field_coordinates,ocr_created_user) VALUES(@templateId,'"
								+ entrys.getKey() + "','" + entrys.getValue() + "','" + auth.getName() + "');");
			}

			Query<?> query = session.createNativeQuery(sql.toString());
			System.out.println("Inserting a new record");
			query.executeUpdate();
			session.getTransaction().commit();
			System.out.println("Insert is completed");

		} catch (Exception ex) {
			System.err.println("Failed to create sessionFactory object." + ex);
			throw new ExceptionInInitializerError(ex);
		} finally {
			// closing the session
			session.close();
		}
	}

}
