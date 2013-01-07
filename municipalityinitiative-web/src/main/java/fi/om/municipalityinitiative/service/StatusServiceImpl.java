package fi.om.municipalityinitiative.service;

import java.lang.management.ManagementFactory;
import java.util.List;

import javax.annotation.Resource;

import org.joda.time.DateTime;
import org.joda.time.LocalDateTime;
import org.joda.time.format.DateTimeFormat;
import org.springframework.transaction.annotation.Transactional;

import com.google.common.base.Optional;
import com.google.common.collect.Lists;

import fi.om.municipalityinitiative.dao.InitiativeDao;
import fi.om.municipalityinitiative.dto.InitiativeSettings;
import fi.om.municipalityinitiative.dto.SchemaVersion;
import fi.om.municipalityinitiative.util.TaskExecutorAspect;
import fi.om.municipalityinitiative.web.Urls;

public class StatusServiceImpl implements StatusService {

    @Resource InitiativeDao initiativeDao;

    @Resource InitiativeSettings initiativeSettings;
    
    @Resource TaskExecutorAspect taskExecutorAspect;

    private final DateTime appStartTime = DateTime.now();
    
    private static final String DATETIME_FORMAT = "yyyy-MM-dd HH:mm:ss.SSS";
    private static final String DATETIME_FORMAT_SHORT = "yyyy-MM-dd HH:mm:ss";

    private final String testEmailSendTo;
    private final boolean testEmailConsoleOutput; 
    private final int  messageSourceCacheSeconds;
    private final boolean testFreemarkerShowErrorsOnPage;
    private final Boolean optimizeResources;
    private final String resourcesVersion;
    private final Optional<Integer> omPiwicId;
    private final String appVersion;

    public class KeyValueInfo {
        private String key;
        private Object value;
        
        public KeyValueInfo(String key, Object value) {
            this.key = key;
            this.value = value;
        }
        
        public String getKey() {
            return key;
        }
        public Object getValue() {
            return value;
        }
    }

    public StatusServiceImpl(String testEmailSendTo, boolean testEmailConsoleOutput,
                             int messageSourceCacheSeconds, boolean testFreemarkerShowErrorsOnPage,
                             Boolean optimizeResources, String resourcesVersion, Optional<Integer> omPiwicId, String appVersion) {
        this.testEmailSendTo = testEmailSendTo;
        this.testEmailConsoleOutput = testEmailConsoleOutput; 
        this.messageSourceCacheSeconds = messageSourceCacheSeconds;
        this.testFreemarkerShowErrorsOnPage = testFreemarkerShowErrorsOnPage;
        this.optimizeResources = optimizeResources;
        this.resourcesVersion = resourcesVersion;
        this.omPiwicId = omPiwicId;
        this.appVersion = appVersion;
    }

    @Override
    @Transactional(readOnly=true)
    public List<KeyValueInfo> getApplicationInfo() {
        List<KeyValueInfo> list = Lists.newArrayList();

        list.add(new KeyValueInfo("appStartTime", appStartTime.toString(DATETIME_FORMAT)));
        list.add(new KeyValueInfo("appVersion", appVersion));
        list.add(new KeyValueInfo("appBuildTimeStamp", getFormattedBuildTimeStamp(resourcesVersion)));
        list.add(new KeyValueInfo("initiativeCount", initiativeDao.getInitiativeCount()));
        list.add(new KeyValueInfo("taskQueueLength", taskExecutorAspect.getQueueLength()));

        return list;
    }
    
    @Override
    @Transactional(readOnly=true)
    public List<KeyValueInfo> getSchemaVersionInfo() {
        List<SchemaVersion> schemaVersions = initiativeDao.getSchemaVersions();
        List<KeyValueInfo> list = Lists.newArrayList();
        
        for (SchemaVersion schemaVersion : schemaVersions) {
            list.add(new KeyValueInfo(schemaVersion.getScript(), schemaVersion.getExecuted().toString(DATETIME_FORMAT)));
        }
        
        return list;
    }

    @Override
    @Transactional(readOnly=true)
    public List<KeyValueInfo> getConfigurationInfo() {
        List<KeyValueInfo> list = Lists.newArrayList();

        list.add(new KeyValueInfo("baseUrl", Urls.FI.getBaseUrl()));
        list.add(new KeyValueInfo("resourcesVersion", resourcesVersion));
        list.add(new KeyValueInfo("optimizeResources", ""+optimizeResources));
        list.add(new KeyValueInfo("omPiwicId", omPiwicId.isPresent()?omPiwicId.orNull():""));

        list.add(new KeyValueInfo("invitationExpirationDays", initiativeSettings.getInvitationExpirationDays()));
        list.add(new KeyValueInfo("requiredMinSupportCountDuration", initiativeSettings.getRequiredMinSupportCountDuration()));
        list.add(new KeyValueInfo("votingDuration", initiativeSettings.getVotingDuration()));
        list.add(new KeyValueInfo("sendToVrkDuration", initiativeSettings.getSendToVrkDuration()));
        list.add(new KeyValueInfo("sendToParliamentDuration", initiativeSettings.getSendToParliamentDuration()));
        list.add(new KeyValueInfo("votesRemovalDuration", initiativeSettings.getVotesRemovalDuration()));
        list.add(new KeyValueInfo("omSearchBeforeVotesRemovalDuration", initiativeSettings.getOmSearchBeforeVotesRemovalDuration()));

        list.add(new KeyValueInfo("minSupportCountForSearch", initiativeSettings.getMinSupportCountForSearch()));
        list.add(new KeyValueInfo("requiredVoteCount", initiativeSettings.getRequiredVoteCount()));
        
        return list;
    }

    @Override
    @Transactional(readOnly=true)
    public List<KeyValueInfo> getConfigurationTestInfo() {
        List<KeyValueInfo> list = Lists.newArrayList();

        list.add(new KeyValueInfo("testEmailSendTo", testEmailSendTo));
        list.add(new KeyValueInfo("testEmailConsoleOutput", ""+testEmailConsoleOutput)); 
        list.add(new KeyValueInfo("testMessageSourceCacheSeconds", messageSourceCacheSeconds));
        list.add(new KeyValueInfo("testFreemarkerShowErrorsOnPage", ""+testFreemarkerShowErrorsOnPage));

        return list;
    }
    
    @Override
    public List<KeyValueInfo> getSystemInfo() {
        List<KeyValueInfo> list = Lists.newArrayList();
        
        list.add(new KeyValueInfo("system time", DateTime.now().toString(DATETIME_FORMAT)));
        list.add(new KeyValueInfo("active threads", Thread.activeCount()));
        list.add(new KeyValueInfo("available processors", Runtime.getRuntime().availableProcessors()));
        list.add(new KeyValueInfo("total memory, Kb", Runtime.getRuntime().totalMemory() / 1024));
        list.add(new KeyValueInfo("max memory, Kb", Runtime.getRuntime().maxMemory() / 1024));
        list.add(new KeyValueInfo("free memory, Kb", Runtime.getRuntime().freeMemory() / 1024));
        
        list.add(new KeyValueInfo("loaded class count", ManagementFactory.getClassLoadingMXBean().getLoadedClassCount()));
        list.add(new KeyValueInfo("nonHeapMemUsage, Kb", ManagementFactory.getMemoryMXBean().getNonHeapMemoryUsage().getUsed() / 1024));
        list.add(new KeyValueInfo("heapMemUsage, Kb", ManagementFactory.getMemoryMXBean().getHeapMemoryUsage().getUsed() / 1024));
        list.add(new KeyValueInfo("OS load avg", ManagementFactory.getOperatingSystemMXBean().getSystemLoadAverage()));
//        list.add(new KeyValueInfo("OS version", ManagementFactory.getOperatingSystemMXBean().getVersion()));
//        list.add(new KeyValueInfo("OS name", ManagementFactory.getOperatingSystemMXBean().getName()));
//        list.add(new KeyValueInfo("OS arch", ManagementFactory.getOperatingSystemMXBean().getArch()));
//        list.add(new KeyValueInfo("default domain", ManagementFactory.getPlatformMBeanServer().getDefaultDomain()));
//        list.add(new KeyValueInfo("boot class path", ManagementFactory.getRuntimeMXBean().getBootClassPath()));
//        list.add(new KeyValueInfo("JVM version", ManagementFactory.getRuntimeMXBean().getVmVersion()));
//        list.add(new KeyValueInfo("JVM vendor", ManagementFactory.getRuntimeMXBean().getVmVendor()));
//        list.add(new KeyValueInfo("JVM name", ManagementFactory.getRuntimeMXBean().getVmName()));
//        list.add(new KeyValueInfo("JVM runtime name", ManagementFactory.getRuntimeMXBean().getName()));
        list.add(new KeyValueInfo("JVM uptime, minutes", ManagementFactory.getRuntimeMXBean().getUptime() / (60 * 1000)));
//        list.add(new KeyValueInfo("JVM input args", ManagementFactory.getRuntimeMXBean().getInputArguments()));
//        list.add(new KeyValueInfo("library path", ManagementFactory.getRuntimeMXBean().getLibraryPath()));
        list.add(new KeyValueInfo("start time JVM", "" + new LocalDateTime(ManagementFactory.getRuntimeMXBean().getStartTime()).toString(DATETIME_FORMAT)));
        
        return list;
    }

    protected static String getFormattedBuildTimeStamp(String resourcesVersion) {
        DateTime buildTimeStamp = getBuildTimeStamp(resourcesVersion);
        if (buildTimeStamp != null) {
            return buildTimeStamp.toString(DATETIME_FORMAT_SHORT); 
        }
        else {
            return "-";
        }
    }
    
    protected static DateTime getBuildTimeStamp(String resourcesVersion) {
        DateTime buildTimeStamp;
        try {
            buildTimeStamp = DateTime.parse(resourcesVersion, DateTimeFormat.forPattern("yyyyMMddHHmmss"));
        } catch (Exception ex) {
            buildTimeStamp = null;
        }
        return buildTimeStamp;
    }
    
    
}