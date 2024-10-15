package guess.util.load;

import guess.domain.source.load.CmsType;
import guess.util.load.contentful.ContentfulDataLoader;
import guess.util.load.jrgcms.JrgCmsDataLoader;

import java.time.LocalDate;

/**
 * Factory of CMS data loader.
 */
public class CmsDataLoaderFactory {
    private static final LocalDate CONTENTFUL_AND_DATE = LocalDate.of(2022, 1, 1);

    private CmsDataLoaderFactory() {
    }

    static CmsDataLoader createDataLoader(CmsType cmsType) {
        if (CmsType.CONTENTFUL.equals(cmsType)) {
            return new ContentfulDataLoader();
        } else if (CmsType.JUGRUGROUP_CMS.equals(cmsType)) {
            return new JrgCmsDataLoader();
        } else {
            throw new IllegalArgumentException(String.format("Unknown CMS type: %s", cmsType));
        }
    }

    static CmsDataLoader createDataLoader(LocalDate startDate) {
        if (startDate.isBefore(CONTENTFUL_AND_DATE)) {
            return new ContentfulDataLoader();
        } else {
            return new JrgCmsDataLoader();
        }
    }
}
