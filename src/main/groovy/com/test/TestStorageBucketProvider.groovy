package com.test

import com.morpheusdata.model.StorageServer
import com.morpheusdata.model.StorageGroup
import com.morpheusdata.model.StorageServerType
import com.morpheusdata.model.StorageBucket
import com.morpheusdata.model.StorageVolumeType
import com.morpheusdata.model.StorageVolume
import com.morpheusdata.model.OptionType
import com.morpheusdata.model.Icon
import com.morpheusdata.core.MorpheusContext
import com.morpheusdata.core.Plugin
import com.morpheusdata.core.providers.StorageProvider
import com.morpheusdata.core.providers.StorageProviderBuckets
import com.morpheusdata.response.ServiceResponse
import com.morpheusdata.core.util.ComputeUtility
import groovy.util.logging.Slf4j
import groovy.json.JsonOutput
import com.test.datasets.TestStorageBucketDatasetProvider

/**
 * TestStorageBucketProvider is a storage bucket provider for Morpheus.
 * It adds a new {@link {StorageServerType}} named 'Test' which can be created
 * to allow its storage buckets to be synced and used as a storage source or destination.
 * This also provides the ability to create and delete buckets and their objects.
 * 
 * @see StorageProvider
 * @see StorageProviderBuckets
 */
@Slf4j
class TestStorageBucketProvider implements StorageProvider, StorageProviderBuckets {
	
	protected MorpheusContext morpheusContext
	protected Plugin plugin

	TestStorageBucketProvider(Plugin plugin, MorpheusContext morpheusContext) {
		this.morpheusContext = morpheusContext
		this.plugin = plugin
	}

    /**
	 * Returns the Morpheus Context for interacting with data stored in the Main Morpheus Application
	 *
	 * @return an implementation of the MorpheusContext for running Future based rxJava queries
	 */
	@Override
	MorpheusContext getMorpheus() {
		return morpheusContext
	}

	/**
	 * Returns the instance of the Plugin class that this provider is loaded from
	 * @return Plugin class contains references to other providers
	 */
	@Override
	Plugin getPlugin() {
		return plugin
	}

	/**
	 * A unique shortcode used for referencing the provided provider. Make sure this is going to be unique as any data
	 * that is seeded or generated related to this provider will reference it by this code.
	 * @return short code string that should be unique across all other plugin implementations.
	 */
	@Override
	String getCode() {
		return "test-storage-buckets"
	}

	/**
	 * Provides the provider name for reference when adding to the Morpheus Orchestrator
	 * NOTE: This may be useful to set as an i18n key for UI reference and localization support.
	 *
	 * @return either an English name of a Provider or an i18n based key that can be scanned for in a properties file.
	 */
	@Override
	String getName() {
		return "Test"
	}

	/**
	 * Provides the provider description
	 * NOTE: This may be useful to set as an i18n key for UI reference and localization support.
	 *
	 * @return either an English name of a Provider or an i18n based key that can be scanned for in a properties file.
	 */
	@Override
    String getDescription() {
        return "Test Storage Bucket Provider"
    }

    /**
	 * Returns the Storage Bucket Integration logo for display when a user needs to view or add this integration
	 * @since 0.12.3
	 * @return Icon representation of assets stored in the src/assets of the project.
	 */
	@Override
	Icon getIcon() {
		return new Icon(path:"morpheus.svg", darkPath: "morpheus.svg")
	}

	/**
	 * Validation Method used to validate all inputs applied to the storage server integration upon save.
	 * If an input fails validation or authentication information cannot be verified, Error messages should be returned
	 * via a {@link ServiceResponse} object where the key on the error is the field name and the value is the error message.
	 * If the error is a generic authentication error or unknown error, a standard message can also be sent back in the response.
	 *
	 * @param storageServer The Storage Server object contains all the saved information regarding configuration of the Storage Provider
	 * @param opts an optional map of parameters that could be sent. This may not currently be used and can be assumed blank
	 * @return A response is returned depending on if the inputs are valid or not.
	 */
	@Override
	ServiceResponse verifyStorageServer(StorageServer storageServer, Map opts) {
		return ServiceResponse.success()
	}

	/**
	 * Called on the first save / update of a storage server integration. Used to do any initialization of a new integration
	 * Often times this calls the periodic refresh method directly.
	 * @param storageServer The Storage Server object contains all the saved information regarding configuration of the Storage Provider.
	 * @param opts an optional map of parameters that could be sent. This may not currently be used and can be assumed blank
	 * @return a ServiceResponse containing the success state of the initialization phase
	 */
	@Override
	ServiceResponse initializeStorageServer(StorageServer storageServer, Map opts) {
		return refreshStorageServer(storageServer, opts)
	}

	/**
	 * Refresh the provider with the associated data in the external system.
	 * This is where the buckets are synced.
	 * @param storageServer The Storage Server object contains all the saved information regarding configuration of the Storage Provider.
	 * @param opts an optional map of parameters that could be sent. This may not currently be used and can be assumed blank
	 * @return a {@link ServiceResponse} object. A ServiceResponse with a success value of 'false' will indicate the
	 * refresh process has failed and will change the storage server status to 'error'
	 */
	@Override
	ServiceResponse refreshStorageServer(StorageServer storageServer, Map opts) {
		ServiceResponse rtn = ServiceResponse.prepare()
		log.debug("refreshStorageServer: ${storageServer.name}")
		try {
			Date syncDate = new Date()
			Boolean hostOnline = true
			if(hostOnline) {
			    // cacheBuckets(storageServer, opts)
			    rtn.success = true

			} else {
			    log.warn("refresh - storageServer: ${storageServer.name} - Storage server appears to be offline")
			    rtn.msg = "Storage server appears to be offline"
			}
		} catch(e) {
			log.error("refreshStorageServer error: ${e}", e)
			rtn.msg = e.message
		}
		println "RTN BE: ${rtn}"
		return rtn
	}

	/**
     * Provides a {@link StorageServerType} record that needs to be configured in the morpheus environment.
     * This record dicates configuration settings and other facets and behaviors for the storage type.
     * @return a {@link StorageServerType}
     */
    @Override
    StorageServerType getStorageServerType() {
        StorageServerType storageServerType = new StorageServerType(
            code:getCode(), name:getName(), description:getDescription(), hasBlock:false, hasObject:true, 
            hasFile:false, hasDatastore:false, hasNamespaces:false, hasGroups:false, hasDisks:true, hasHosts:false,
            createBlock:true, createObject:true, createFile:false, createDatastore:true, createNamespaces:false, 
            createGroup:true, createDisk:false, createHost:false, hasFileBrowser: true)
        storageServerType.optionTypes = getStorageServerOptionTypes()
        storageServerType.volumeTypes = getVolumeTypes()
        storageServerType.bucketOptionTypes = getStorageBucketOptionTypes()
        return storageServerType
    }
    
    /**
     * Provide custom configuration options when creating a new {@link StorageServer}
     * @return a List of OptionType
     */
    Collection<OptionType> getStorageServerOptionTypes() {
        return [
            new OptionType(code: 'test.credential', name: 'Credentials', inputType: OptionType.InputType.CREDENTIAL, fieldName: 'type', fieldLabel: 'Credentials', fieldContext: 'credential', required: true, displayOrder: 10, defaultValue: 'local',optionSource: 'credentials',config: '{"credentialTypes":["username-password"]}'),
            new OptionType(code: 'test.serviceUsername', name: 'Username', inputType: OptionType.InputType.TEXT, fieldName: 'serviceUsername', fieldLabel: 'Username', fieldContext: 'domain', required: true, displayOrder: 11,localCredential: true),
            new OptionType(code: 'test.servicePassword', name: 'Password', inputType: OptionType.InputType.PASSWORD, fieldName: 'servicePassword', fieldLabel: 'Password', fieldContext: 'domain', required: true, displayOrder: 12,localCredential: true),
            new OptionType(code: 'test.serviceUrl', name: 'Endpoint', inputType: OptionType.InputType.TEXT , fieldName: 'serviceUrl', fieldLabel: 'Endpoint', fieldContext: 'domain', required: false, displayOrder: 13)
        ]
    }

    /**
     * Provides a collection of {@link VolumeType} records that needs to be configured in the morpheus environment
     * @return Collection of StorageVolumeType
     */
    Collection<StorageVolumeType> getVolumeTypes() {
        return [
            // new StorageVolumeType(code:'testObject', displayName:'Test Object Storage', name:'Test Object Storage', description:'Test Object Storage', volumeType:'bucket', enabled:false, displayOrder:1, customLabel:true, customSize:true, defaultType:true, autoDelete:true, deletable:true, resizable:true, minStorage:ComputeUtility.ONE_GIGABYTE, maxStorage:(10L * ComputeUtility.ONE_TERABYTE), allowSearch:true, volumeCategory:'volume', optionTypes:[
            // 	    new OptionType(code: 'test-volumeThing', name: 'Volume Thing', inputType: OptionType.InputType.TEXT, fieldName: 'config.volumeThing', fieldLabel: 'Volume Thing', fieldContext: 'domain', required: true, displayOrder: 1, editable: false, fieldCode:'Volume Thing'),
            // 	    new OptionType(code: 'test-cloud', name: 'Cloudy', inputType: OptionType.InputType.MULTI_SELECT, fieldName:'blahblah', fieldLabel: 'Cloudy', fieldContext: 'config', required: true, displayOrder: 2, editable: false, fieldCode:'Cloudy', optionSource: 'clouds'),
            // 	    new OptionType(code: 'test-group', name: 'Group', inputType: OptionType.InputType.MULTI_SELECT, fieldName:'group', fieldLabel: 'Group', fieldContext: 'config', required: true, displayOrder: 3, editable: false, fieldCode:'Group', optionSource: 'groups'),
            // 	    // Resizable OptionTypes
			// 		new OptionType(
			// 			name: 'Test Sample Source',
			// 			code: 'test-sample-source',
			// 			displayOrder: 0,
			// 			fieldContext: 'config',
			// 			fieldName: 'testSampleSource',
			// 			fieldCode: 'Test Sample Source',
			// 			inputType: OptionType.InputType.MULTI_SELECT,
			// 			config: JsonOutput.toJson(resizable:true).toString(),
			// 			optionSourceType: TestStorageBucketDatasetProvider.providerNamespace,
			// 			optionSource: TestStorageBucketDatasetProvider.providerKey,
			// 		)

			// ])
			new StorageVolumeType(code:'testObject', displayName:'Test Object Storage', name:'Test Object Storage', description:'Test Object Storage', volumeType:'bucket', enabled:false, displayOrder:1, customLabel:true, customSize:true, defaultType:true, autoDelete:true, deletable:true, resizable:true, minStorage:ComputeUtility.ONE_GIGABYTE, maxStorage:(10L * ComputeUtility.ONE_TERABYTE), allowSearch:true, volumeCategory:'volume', optionTypes:[])
        ]
    }

    /**
     * Provide custom configuration options when creating a new {@link StorageBucket}
     * @return a List of OptionType
     */
    Collection<OptionType> getStorageBucketOptionTypes() {
        return [
            new OptionType(code: 'test.bucketName', name: 'Bucket Name', inputType: OptionType.InputType.TEXT, fieldName: 'bucketName', fieldLabel: 'Bucket Name', fieldContext: 'domain', required: true, displayOrder: 1, editable: false, fieldCode:'gomorpheus.label.bucketName'),
            new OptionType(code: 'test.createBucket', name: 'Create Bucket', inputType: OptionType.InputType.CHECKBOX, fieldName: 'createBucket', fieldLabel: 'Create Bucket', fieldContext: null, required: false, displayOrder: 2, fieldCode:'gomorpheus.label.createBucket')
        ]
    }


    /**
	 * Validates the submitted bucket information.
	 * This is invoked before both {@link #createBucket} and {@link #updateBucket}.
	 * @param storageBucket Storage Bucket information
	 * @param opts additional options
	 * @return a {@link ServiceResponse} object. The errors field of the ServiceResponse is used to send validation
	 * results back to the interface in the format of {@code errors['fieldName'] = 'validation message' }. The msg
	 * property can be used to send generic validation text that is not related to a specific field on the model.
	 * A ServiceResponse with any items in the errors list or a success value of 'false' will halt the backup creation
	 * process.
	 */
	ServiceResponse validateBucket(StorageBucket storageBucket, Map opts) {
		return ServiceResponse.success()
	}
	
	/**
	 * Create the {@link StorageBucket} resources on the external provider system.
	 * @param storageBucket the fully configured and validated bucket to be created
	 * @param opts additional options
	 * @return a {@link ServiceResponse} object. A ServiceResponse with a success value of 'false' will indicate the
	 * creation on the external system failed and prevent the bucket creation in morpheus.
	 */
	ServiceResponse createBucket(StorageBucket storageBucket, Map opts) {
		return ServiceResponse.success()
	}

	/**
	 * Called during update of an existing {@link StorageBucket}. This allows for any custom operations that need
	 * to be performed outside of the standard operations.
	 * @param storageBucket the storage bucket to be updated
	 * @param opts additional options
	 * @return a {@link ServiceResponse} object. A ServiceResponse with a success value of 'false' will indicate the
	 * update on the external system failed and will halt the bucket update process and leave the bucket unmodified.
	 */
	ServiceResponse updateBucket(StorageBucket storageBucket, Map opts) {
		return ServiceResponse.success()
	}

	/**
	 * Delete the {@link StorageBucket} resources on the external provider system.
	 * @param storageBucket the storage bucket details
	 * @param opts additional options
	 * @return a {@link ServiceResponse} indicating the results of the deletion on the external provider system.
	 * A ServiceResponse object with a success value of 'false' will halt the deletion process and the local reference
	 * will be retained.
	 */
	ServiceResponse deleteBucket(StorageBucket storageBucket, Map opts) {
		return ServiceResponse.success()
	}

	/**
	 * Provides a list of supported storage provider types.
	 * The available codes are: ['alibaba','azure','cifs','google',local','nfs','openstack','s3']
	 * @return Collection of provide type codes
	 */
	Collection<String> getStorageBucketProviderTypes() {
        ["s3"]
    }

    def createVolume(StorageServer storageServer, StorageVolume storageVolume, Map opts){

    	println "${"\u001B[33m"} the opts are: ${opts}  ${"\u001B[0m"}"
    	storageVolume = morpheusContext.services.storageVolume.create(storageVolume)
    	println "${"\u001B[33m"} the storageVolume ID IS: ${storageVolume.id}  ${"\u001B[0m"}"
    	return [success: true]
    }

    def deleteVolume(StorageServer storageServer, StorageVolume storageVolume, Map opts){
    	println "${"\u001B[33m"}Im deleting storageServer: ${storageServer}  storageVolume: ${storageVolume}, opts: ${opts}${"\u001B[0m"}"
    	return [success: true]
    }

    ServiceResponse<StorageVolume> resizeVolume(StorageServer storageServer, StorageVolume storageVolume, Map opts){
    	println "${"\u001B[33m"}opts: ${opts.opts.config.testSampleSource}${"\u001B[0m"}"

    	storageVolume.maxStorage = opts.maxStorage.toLong()

		// modify the config map to set an array of values
		if(opts.opts.config.testSampleSource instanceof String){
			storageVolume.setConfigProperty('testSampleSource', [opts.opts.config.testSampleSource])
		}
		if(opts.opts.config.testSampleSource instanceof List){
			storageVolume.setConfigProperty('testSampleSource', opts.opts.config.testSampleSource)
		}



		println "${"\u001B[33m"}The result: ${storageVolume} ${"\u001B[0m"}"
    	return ServiceResponse.create([success: true, data: [storageVolume: storageVolume]])
    }

}
