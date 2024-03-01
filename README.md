# <u>RLCM - Reinforcement Learning-based Multi-tier Cache Management Framework</u>

RLCM is a reinforcement learning (RL)-based cache management framework that can be integrated with a multi-tier cloud caching service for making all cache-related decisions, including initial data placement, data upgrades to higher tiers, data downgrades to lower tiers, and data evictions from tiers. RLCM is designed and implemented as a standalone component. For validation purposes, we integrated RLCM with the [Smart Cloud Caching System (SMACC)](https://github.com/cut-dicl/smacc) that runs on application compute nodes (e.g., on Amazon EC2) and caches frequently-used data residing on cloud storage (e.g., Amazon S3) into a multi-tier cache of memory and locally-attached disks.


***

## RLCM Key Components

The cache management model, based on Reinforcement Learning (RL), is designed with three basic components: the RL Manager and the two RL Agents (Admission Agent and Eviction Agent).

1. **RL Manager**: The RL Manager oversees the overall process of collecting request information from the caching system, storing it in a Metadata Repository for computing the environment states, managing the Admission and Eviction Agents, collecting usage statistics from the cache tiers, as well as computing rewards.

2. **Admission Agent**: The Admission Agent is tasked with selectively admitting and upgrading the most relevant and high-priority data into the cache tiers, ensuring an optimal balance between cache utilization and performance.

3. **Eviction Agent**: The Eviction Agent is tasked with strategically evicting less relevant or infrequently accessed items while retaining valuable ones when a cache tier reaches capacity, as well as deciding whether to downgrade or remove the evicted items from the cache.


***

## RLCM with SMACC Caching System

SMACC is a novel Cloud caching service that can run on application compute nodes (e.g., on Amazon EC2) and cache frequently-used data residing on cloud storage (e.g., Amazon S3, MinIO) into local memory and locally-attached disks (e.g., Amazon EBS).

Integrating RLCM with SMACC involved implementing three pluggable cache policy interfaces exposed by SMACC for admitting, evicting, and downgrading files from the multi-tier cache.

1. The implemented admission policy makes admission decisions on write (i.e., placement) and on read (i.e., upgrade) according to our RL-based decisions of the Admission Agent. 

2. The implemented eviction policy decides which file(s) to evict from a given cache tier according to our RL-based decisions of the Eviction Agent.

3. The implemented downgrade policy decides whether to downgrade or delete the evicted file, also according to our RL-based decisions of the Eviction Agent.

**Note**: You can download and integrate RLCM into any other cloud caching service that can run on application compute nodes. You will need to implement the appropriate RL-based caching policies and import the necessary configuration file or parameters in cachemanager.config.properties file.


***

## Installation

Using Debian/Ubuntu (Ubuntu 20.04.2 LTS +) or Windows 10

### Prerequisites

General Prerequisites:
- Java 15
- [Apache Maven 3.6.3+] (sudo apt install maven)
- [SMACC 2.0](https://github.com/cut-dicl/smacc)

### Compilation

You need to first download and compile SMACC based on the instruction from [https://github.com/cut-dicl/smacc](https://github.com/cut-dicl/smacc)

From SMACC's source code directory, execute:

```bash
mvn clean package install
```

Then, move to RLCM's source code repository and build jar using **MVN**

```bash
mvn clean package -U
```


***

## Configurations

There are many configuration options available, in the cache manager properties file for fine-tuning the RL-based cache management system's behavior according to specific requirements, including the number of tiers, QModel settings, maximum update limits, and cache capacity settings.

### cachemanager.config.properties

```properties
#Tiers configurations
cachemanager.qmodelcache.tiers: This parameter specifies the number of tiers in the caching hierarchy (e.g., 1).
cachemanager.existSSD: This parameter specifies the existence (true) or not (false) of SSD tier in cache.
cachemanager.existDISK: This parameter specifies the existence (true) or not (false) of disk tier in cache.

#QModel configurations
cachemanager.qmodelcache.alpha: Alpha (Learning Rate) - This value determines the extent to which the agent updates its Q-values in response to new information (e.g., 0.1).
cachemanager.qmodelcache.gamma: Gamma (Discount Factor) - Determines the agent's consideration of future rewards in its decision-making process (e.g., 0.7).
cachemanager.qmodelcache.admission.initialQs: Initialise Admission Qvalues for each cache tier and combination in order [ NONE, SSD, MEM, SSD_MEM ] (e.g., 0.01, 0.3, 0.4, 0.2). 
cachemanager.qmodelcache.replacement.initialQs: Initialise Replacement Qvalues for not eviction, downgrade and eviction in order [ NOT_EVICT, EVICT_TO_LOWER_TIER, EVICT_TO_NONE ] (e.g., 0.2, 0.3, 0.01).
cachemanager.maximumupdatesnum.updatestrategy: This parameter specifies the maximum updates (#cache functions) for update strategy to limit the number of updates during the update strategy phase (e.g., 100).
cachemanager.maximumupdates.decisions: This parameter specifies the maximum updates for decisions (then the desicion deleted) by setting a limit to updates during the decision-making process (e.g., 5).
cachemanager.qmodelreplacement.maxLRU: This parameter specifies the number of least recently used files which take priority to selected for evict when trigger an eviction request (e.g., 100).

#Cache configurations
cachemanager.percentage.memorycapacitythreshold: This parameter specifies the threshold's value (percentage) to trigger cache management actions when memory cache tier utilization exceeds this threshold. (e.g., 85)
cachemanager.memory.capacity: This parameter specifies the total capacity (in bytes) of the memory cache tier (e.g., 1000000)
cachemanager.percentage.diskcapacitythreshold: This parameter specifies the threshold's value (percentage) to trigger some disk cache management actions when disk cache tier utilization exceeds this threshold. (e.g., 85)
cachemanager.disk.capacity: This parameter specifies the total capacity (in bytes) of the disk cache tier (e.g., 1000000000)

```


***

## Usage

These are the usage instuctions for running RLCM with SMACC.

### Start the SMACC server and use it with the SmaccClientCLI

1. In 'server.config.properties' you need to specify the path location of your 'cachemanager.config.properties' file.

```bash
rl.config.file = path/to/cachemanager.config.properties
```

2. Start the SMACC server using **java**

```bash
java -cp path/to/RLCM-1.0-jar-with-dependencies.jar:path/to/smacc-2.0-jar-with-dependencies.jar edu.cut.smacc.server.main.ServerMain -c server.config.properties
```

3. Start the SMACC client using **java** and the appropriate options

```bash
java -cp target/smacc-2.0-jar-with-dependencies.jar edu.cut.smacc.client.SmaccClientCLI -c client.config.properties -h
```

***

## Funding
- "POST-DOCTORAL" Research Programme of the Cyprus University of Technology (ML-SMACC), August 2022 - August 2023
- AWS Cloud Credits for Research Grant, Amazon Web Services, October 2022


## Contact
- Elena Kakoulli, Neapolis University Pahos, [https://www.nup.ac.cy/faculty/elena-kakoulli/](https://www.nup.ac.cy/faculty/elena-kakoulli/)
- Herodotos Herodotou, Cyprus University of Technology, [https://dicl.cut.ac.cy/](https://dicl.cut.ac.cy/)

