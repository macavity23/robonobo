package com.robonobo.sonar;

import static com.robonobo.common.util.TimeUtil.*;

import java.util.*;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.hibernate.*;
import org.hibernate.criterion.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.orm.hibernate3.support.HibernateDaoSupport;
import org.springframework.stereotype.Repository;

import com.robonobo.core.api.proto.CoreApi.EndPoint;
import com.robonobo.core.api.proto.CoreApi.Node;
import com.robonobo.sonar.beans.SonarEndPoint;
import com.robonobo.sonar.beans.SonarNode;

@Repository("nodeDao")
public class NodeDaoImpl extends HibernateDaoSupport implements NodeDao {
	Log log = LogFactory.getLog(getClass());
	
	@Override
	public List<Node> getAllSupernodes(Node except) {
		Criteria criteria = getSession().createCriteria(SonarNode.class);
		criteria.add(Restrictions.eq("supernode", true));
		if(except != null)
			criteria.add(Restrictions.ne("id", except.getId()));
		criteria.add(Restrictions.gt("lastSeen", new Date(new Date().getTime() - 300000)));
		criteria.addOrder(Order.desc("lastSeen"));
		List<SonarNode> sonarNodes = criteria.list();
		List<Node> result = new ArrayList<Node>();
		for (SonarNode sn : sonarNodes) {
			result.add(sn.toMsg());
		}
		return result;
	}
	
	@Override
	public List<Node> getPublicNodes(int maxNum) {
		String hql = "select ep.node from SonarEndPoint as ep where ep.url not like '%nt' order by ep.node.lastSeen desc";
		Query q = getSession().createQuery(hql);
		List<SonarNode> snList = q.list();
		List<Node> result = new ArrayList<Node>();
		for (SonarNode sn : snList) {
			result.add(sn.toMsg());
		}
		return result;
	}
	
	@Override
	public void deleteAllNodes() {
		Session s = getSession();
		Criteria c = s.createCriteria(SonarNode.class);
		Iterator i = c.list().iterator();
		while(i.hasNext()) {
			Object node = i.next();
			s.delete(node);
		}		
	}
	
	@Override
	public void deleteDuplicateNodes(Node n) {
		Set<SonarNode> nodesToDelete = new HashSet<SonarNode>();
		for (EndPoint ep : n.getEndPointList()) {
			Criteria crit = getSession().createCriteria(SonarEndPoint.class);
			crit.add(Restrictions.eq("url", ep.getUrl()));
			List<SonarEndPoint> matchingEps = crit.list();
			for (SonarEndPoint mEp : matchingEps) {
				nodesToDelete.add(mEp.getNode());
				getSession().delete(mEp);
			}
		}
		for (SonarNode nd : nodesToDelete) {
			getSession().delete(nd);
		}
	}
	
	@Override
	public void saveNode(Node n) {
		SonarNode sn = getSonarNode(n.getId());
		if(sn != null)
			getSession().delete(sn);
		sn = new SonarNode(n);
		sn.setLastSeen(now());
		getSession().saveOrUpdate(sn);
	}
	
	private SonarNode getSonarNode(String id) {
		Criteria crit = getSession().createCriteria(SonarNode.class);
		crit.add(Restrictions.eq("id", id));
		return (SonarNode) crit.uniqueResult();
	}
	@Override
	public void deleteNodesOlderThan(long maxAgeMs) {
		Date date = timeInPast(maxAgeMs);
		Criteria criteria = getSession().createCriteria(SonarNode.class);
		criteria.add(Restrictions.lt("lastSeen", date));
		List nodes = criteria.list();
		Iterator i = nodes.iterator();
		while(i.hasNext())
			getSession().delete(i.next());
		if(nodes.size() > 0)
			log.info(nodes.size()+" old nodes deleted");
	}
	
	@Autowired
	public void injectSessionFactory(SessionFactory sessionFactory) {
		setSessionFactory(sessionFactory);
	}
}
