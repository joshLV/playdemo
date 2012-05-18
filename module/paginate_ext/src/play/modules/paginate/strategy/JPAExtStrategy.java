package play.modules.paginate.strategy;

import org.apache.commons.lang.StringUtils;
import play.Play;
import play.db.jpa.JPA;
import play.exceptions.UnexpectedException;

import javax.persistence.Entity;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceUnit;
import javax.persistence.Query;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.List;
import java.util.Map;

/**
 * JPA翻页处理策略.
 * <p/>
 * User: sujie
 * Date: 3/5/12
 * Time: 4:41 PM
 */
public class JPAExtStrategy<T> extends JPARecordLocatorStrategy {
    private Class<T> typeToken;
    private Map<String, Object> paramMap;
    private String filter;
    private String key = "id";
    // set when we are filtering the queries to a specific list of ids
    private String keyFilter;
    private static final String SELECT = "SELECT ";
    private boolean useQueryCache;
    private Object[] params;
    private String entityName;
    private String select;

    private String groupBy;

    public JPAExtStrategy(Class<T> typeToken) {
        super(typeToken);
        this.typeToken = typeToken;
        String useQueryCacheStr = Play.configuration.getProperty("paginator.jpa.useQueryCache", "true");
        this.useQueryCache = Boolean.parseBoolean(useQueryCacheStr);
    }

    public JPAExtStrategy(Class<T> typeToken, List keys) {
        this(typeToken);
        this.typeToken = typeToken;
        String preparedStatementParameters = StringUtils.repeat("?,", keys.size());
        preparedStatementParameters = preparedStatementParameters.substring(0, preparedStatementParameters.length() - 1);
        this.keyFilter = "IN (" + preparedStatementParameters + ")";
        this.params = keys.toArray();
    }

    public JPAExtStrategy(Class<T> typeToken, String filter, Object... params) {
        this(typeToken);
        this.filter = filter;
        this.params = params;
    }

    public JPAExtStrategy(Class<T> typeToken, String filter, Map<String,
            Object> paramMap, String entityName, String select) {
        this(typeToken);
        this.filter = filter;
        this.paramMap = paramMap;
        this.entityName = entityName;
        this.select = select;
    }

    @Override
    public int count() {
        return ((Long) query("COUNT(*)", false, false).getSingleResult()).intValue();
    }

    @Override
    public List<T> fetchPage(int startRowIdx, int lastRowIdx) {
        List<T> pageValues = findByIndex(startRowIdx, lastRowIdx);
        return pageValues;
    }

    private List<T> findByIndex(int firstRowIdx, int lastRowIdx) {
        int pageSize = lastRowIdx - firstRowIdx;
        return query(this.select, true, true).setFirstResult(firstRowIdx).setMaxResults(pageSize).getResultList();
    }

    protected Query query(String select, boolean applyOrderBy) {
        return query(select, applyOrderBy, true);
    }

    protected Query query(String select, boolean applyOrderBy, boolean applyGroupBy) {
        StringBuilder hql = new StringBuilder();
        if (select != null) {
            if (!select.regionMatches(true, 0, SELECT, 0, SELECT.length()))
                hql.append("SELECT ");
            hql.append(select);
            hql.append(' ');
        }
        hql.append("FROM " + getEntityName());
        if (filter != null) {
            hql.append(" WHERE " + filter);
        } else if (keyFilter != null) {
            hql.append(" WHERE " + key + " " + keyFilter);
        }
        if (applyGroupBy) {
            if (getGroupBy() != null) {
                hql.append(" GROUP BY " + getGroupBy());
            }
        }
        if (applyOrderBy) {
            if (getOrderBy() != null) {
                hql.append(" ORDER BY " + getOrderBy());
            }
        }
        EntityManager em = JPA.em();
        // Play! <= 1.2.3 did not have built-in support for multiple databases...
        // To ensure we are backwards compatible, we use reflection to check that this
        // API is available, so this code still works for people using Play! <= 1.2.3.
        if (typeToken.isAnnotationPresent(PersistenceUnit.class)) {
            String unitName = typeToken.getAnnotation(PersistenceUnit.class).name();
            try {
                Method getJPAConfigMethod = JPA.class.getMethod("getJPAConfig", String.class);
                // guard: only call this code if the user is using a version of Play! that
                // has the static getJPAConfig method on the JPA class (Play! <= 1.2.3 does not)
                if (getJPAConfigMethod != null) {
                    // use reflection to support Play! <= 1.2.3
                    //em = JPA.getJPAConfig(unitName).getJPAContext().em();
                    Object config = getJPAConfigMethod.invoke(JPA.class, unitName);
                    Method getJPAContextMethod = config.getClass().getMethod("getJPAContext");
                    Object context = getJPAContextMethod.invoke(config);
                    Method emMethod = context.getClass().getMethod("em");
                    em = (EntityManager) emMethod.invoke(context);
                }
            } catch (SecurityException e) {
                // checked exceptions are stupid
                throw new UnexpectedException(e);
            } catch (IllegalArgumentException e) {
                // checked exceptions are stupid
                throw new UnexpectedException(e);
            } catch (NoSuchMethodException e) {
                // checked exceptions are still stupid
                throw new UnexpectedException(e);
            } catch (IllegalAccessException e) {
                // checked exceptions are still stupid
                throw new UnexpectedException(e);
            } catch (InvocationTargetException e) {
                // checked exceptions are still stupid
                throw new UnexpectedException(e);
            }
        }

        System.out.println("hql.toString():" + hql.toString());
        Query query = em.createQuery(hql.toString());
        if (useQueryCache) {
            query.setHint("org.hibernate.cacheable", true);
        }
        if (paramMap != null) {
            for (String param : paramMap.keySet()) {
                query.setParameter(param, paramMap.get(param));
            }
        }
        return query;
    }

    public String getEntityName() {
        if (this.entityName != null) {
            return this.entityName;
        }
        String entityName = typeToken.getAnnotation(Entity.class).name();
        if (entityName.length() == 0) {
            entityName = typeToken.getSimpleName();
        }
        return entityName;
    }


    public String getGroupBy() {
        return groupBy;
    }

    public void setGroupBy(String groupBy) {
        this.groupBy = groupBy;
    }
}
