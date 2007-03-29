package jp.naist.se.stigmata;

/*
 * $Id$
 */

import jp.naist.se.stigmata.filter.Criterion;
import jp.naist.se.stigmata.spi.ComparisonPairFilterSpi;

/**
 * Filtering results by some criteria.
 * For example,
 * <ul>
 *   <li>extract comparison pairs which similarity over 0.8, and</li>
 *   <li>extract comparison pairs which similarity over 0.8 and element count over 10.</li>
 * </ul>
 *
 * @author Haruaki TAMADA
 * @version $Revision$ $Date$
 */
public interface ComparisonPairFilter{
    public boolean isFiltered(ComparisonPair pair);

    public Criterion[] getAcceptableCriteria();

    public boolean isAcceptable(Criterion criterion);

    public void setCriterion(Criterion criterion);

    public Criterion getCriterion();

    public ComparisonPairFilterSpi getService();
}