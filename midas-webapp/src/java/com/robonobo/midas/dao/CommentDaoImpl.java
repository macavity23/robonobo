package com.robonobo.midas.dao;

import java.util.Date;
import java.util.List;

import org.hibernate.Hibernate;
import org.hibernate.Query;
import org.springframework.stereotype.Repository;

import com.robonobo.midas.model.MidasComment;

@Repository("commentDao")
@SuppressWarnings({ "unchecked", "rawtypes" })
public class CommentDaoImpl extends MidasDao implements CommentDao {
	@Override
	public MidasComment getComment(long commentId) {
		return (MidasComment) getSession().get(MidasComment.class, commentId);
	}

	@Override
	public long getHighestCommentId() {
		String hql = "select max(commentId) from MidasComment";
		List l = getSession().createQuery(hql).list();
		if (l.size() == 0 || l.get(0) == null)
			return 0;
		return ((Long) l.get(0)).longValue();
	}

	@Override
	public void deleteComment(MidasComment comment) {
		getSession().delete(comment);
	}

	@Override
	public void deleteAllComments(String resourceId) {
		String hql = "delete MidasComment where resourceId = :resourceId";
		getSession().createQuery(hql).setString("resourceId", resourceId).executeUpdate();
	}

	@Override
	public void saveComment(MidasComment comment) {
		getSession().save(comment);
	}

	@Override
	public List<MidasComment> getAllComments(String resourceId) {
		String hql = "from MidasComment where resourceId = :resId";
		return getSession().createQuery(hql).setString("resId", resourceId).list();
	}

	@Override
	public List<MidasComment> getCommentsSince(String resourceId, Date since) {
		String hql = "from MidasComment where resourceId = :resId and date > :since";
		Query q = getSession().createQuery(hql);
		q.setString("resId", resourceId);
		q.setParameter("since", since, Hibernate.TIMESTAMP);
		return q.list();
	}
}
