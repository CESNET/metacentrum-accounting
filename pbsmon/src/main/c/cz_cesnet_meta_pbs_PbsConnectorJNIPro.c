#include <jni.h>
#include "cz_cesnet_meta_pbs_PbsConnectorJNI.h"
#include <stdio.h>
#include <stdlib.h>
#include <pbs_error.h>
#include <pbs_ifl.h>
#include <unistd.h>
#include <signal.h>

extern char *pbs_server;

/* helper function */
jobjectArray buildArrayPro(struct batch_status *bs,JNIEnv *env,jclass infoObj_class,jclass pbsException_class,jmethodID pbsExceptionConstructor,jmethodID internId);

/*
 * cz.cesnet.meta.pbs.PbsConnectorJNI class
 * void loadInfo() 
 */
JNIEXPORT void JNICALL Java_cz_cesnet_meta_pbs_PbsConnectorJNI_loadInfoPro (JNIEnv *env, jobject this) {
 /*JNI variables*/
 jclass this_class,pbsException_class,string_class;
 jclass node_class,server_class,queue_class,job_class;
 jfieldID pbsServerId;
 jfieldID nodes_fieldId,jobs_fieldId,queues_fieldId,servers_fieldId;
 jobjectArray nodes,queues,jobs,servers;
 jmethodID pbsExceptionConstructor,internId;
 jstring pbsServer;
 /*PBS variables*/
 char *errmsg;
 int con;
 int res;
 struct batch_status *bs;
 struct sigaction pbssig, oldpbssig;

 /* get Exception class and constructor */
 pbsException_class = (*env)->FindClass(env, "java/lang/RuntimeException");
 if (pbsException_class == 0) { return; }
 pbsExceptionConstructor = (*env)->GetMethodID(env, pbsException_class, "<init>", "(Ljava/lang/Throwable;)V");
 if (pbsExceptionConstructor == 0) { return; }

 /* get String.intern() method */
 string_class = (*env)->FindClass(env, "java/lang/String");
 if(string_class == 0) { return; }
 internId  = (*env)->GetMethodID(env,string_class,"intern","()Ljava/lang/String;");
 if(internId == 0) { return; }

 /* pbsServer field */
 this_class = (*env)->GetObjectClass(env,this);;
 pbsServerId = (*env)->GetFieldID(env, this_class, "pbsServer", "Ljava/lang/String;");
 if (pbsServerId == 0) { return; }
 pbsServer = (*env)->GetObjectField(env, this, pbsServerId);

 /* remove JVM's handler for SIGCHL */
 pbssig.sa_handler=SIG_DFL;
 pbssig.sa_flags=0;
 sigaction(SIGCHLD, &pbssig, &oldpbssig);

 /* connect */
 if(pbsServer!=NULL) {
  const char *serverUTF8 = (*env)->GetStringUTFChars(env, pbsServer, (jboolean *)0);
  con = pbs_connect((char*)serverUTF8);
  (*env)->ReleaseStringUTFChars(env, pbsServer, serverUTF8);
 } else {
  con = pbs_connect(NULL);
 }
 /* return SIGCHLD handler */
 sigaction(SIGCHLD, &oldpbssig, &pbssig);
   
 /* throw exception if not connected */
 if(con<0) {
    char *buf;
    asprintf(&buf, "pbs_connect(\"%s\"): Error %d viz https://github.com/PBSPro/pbspro/blob/master/src/include/pbs_error.h \n",pbs_server,pbs_errno);
    (*env)->ThrowNew(env, pbsException_class, buf); 
    free(buf);
    return;
 }

 /* get Node class */
 node_class = (*env)->FindClass(env,"cz/cesnet/meta/pbs/Node");
 if( (*env)->ExceptionOccurred(env) != NULL) return;
 /* query the server */
 bs = pbs_statnode(con, "", NULL, NULL);
 /* build array of java objects from response */
 nodes = buildArrayPro(bs,env,node_class,pbsException_class,pbsExceptionConstructor,internId);
 if(nodes==NULL) return;
 /* PBSPro.nodes field */
 nodes_fieldId = (*env)->GetFieldID(env, this_class, "nodes", "[Lcz/cesnet/meta/pbs/Node;");
 if (nodes_fieldId == 0) { return; }
 /* set the field */
 (*env)->SetObjectField(env,this,nodes_fieldId,nodes);

 /* get Job class */
 job_class = (*env)->FindClass(env,"cz/cesnet/meta/pbs/Job");
 if( (*env)->ExceptionOccurred(env) != NULL) return;
 /* query the server: t - job arrays, x - finished jobs*/
 bs = pbs_statjob(con, "", NULL, "tx");
 /* build array of java objects from response */
 jobs = buildArrayPro(bs,env,job_class,pbsException_class,pbsExceptionConstructor,internId);
 if(jobs==NULL) return;
 /* PBSPro.jobs field */
 jobs_fieldId = (*env)->GetFieldID(env, this_class, "jobs", "[Lcz/cesnet/meta/pbs/Job;");
 if (jobs_fieldId == 0) { return; }
 /* set the field */
 (*env)->SetObjectField(env,this,jobs_fieldId,jobs);

 /* get JobQueue class */
 queue_class = (*env)->FindClass(env,"cz/cesnet/meta/pbs/Queue");
 if( (*env)->ExceptionOccurred(env) != NULL) return;
 /* query the server */
 bs = pbs_statque(con, "", NULL, NULL);
 /* build array of java objects from response */
 queues = buildArrayPro(bs,env,queue_class,pbsException_class,pbsExceptionConstructor,internId);
 if(queues==NULL) return;
 /* PBSPro.queues field */
 queues_fieldId = (*env)->GetFieldID(env, this_class, "queues", "[Lcz/cesnet/meta/pbs/Queue;");
 if (queues_fieldId == 0) { return; }
 /* set the field */
 (*env)->SetObjectField(env,this,queues_fieldId,queues);

 /* get PBSServer class */
 server_class = (*env)->FindClass(env,"cz/cesnet/meta/pbs/PbsServer");
 if( (*env)->ExceptionOccurred(env) != NULL) return;
 /* query the server */
 bs = pbs_statserver(con, NULL, NULL);
 /* build array of java objects from response */
 servers = buildArrayPro(bs,env,server_class,pbsException_class,pbsExceptionConstructor,internId);
 if(servers==NULL) return;
 /* PBSPro.server field */
 servers_fieldId = (*env)->GetFieldID(env, this_class, "servers", "[Lcz/cesnet/meta/pbs/PbsServer;");
 if (servers_fieldId == 0) { return; }
 /* set the field */
 (*env)->SetObjectField(env,this,servers_fieldId,servers);

 /* end connection */
 pbs_disconnect(con);
}

jobjectArray buildArrayPro(struct batch_status *bs,JNIEnv *env,jclass infoObj_class,jclass pbsException_class,jmethodID pbsExceptionConstructor,jmethodID internId) {
 struct batch_status *tmp;
 struct attrl *atp;
 jsize pocet = 0;
 jobject obj,attrsMap;
 jobjectArray objs;
 jstring objName;
 jclass attrsClass;
 jfieldID attrsId;
 jmethodID infoObjConstructor,putId;
 jthrowable exc;
 int i;

 /* get PBSInfoObject(String) constructor */
 infoObjConstructor = (*env)->GetMethodID(env, infoObj_class, "<init>", "(Ljava/lang/String;)V");
 if (infoObjConstructor == 0) { return NULL; }

 /* get PBSInfoObject.attrs field */
 attrsId = (*env)->GetFieldID(env, infoObj_class, "attrs", "Ljava/util/TreeMap;");
 if (attrsId == 0) { return; }

 /* count results */
 for(tmp=bs;tmp!=NULL;tmp=tmp->next) { pocet++; }

 /* create array */
 objs = (*env)->NewObjectArray(env,pocet,infoObj_class,NULL);
 if( (*env)->ExceptionOccurred(env) != NULL) return NULL;

 /* build Java objects from C data structure */
 for(tmp=bs,i=0;tmp!=NULL;tmp=tmp->next,i++) {
   /*new PBSInfo object*/
   objName = (*env)->NewStringUTF(env, tmp->name);
   obj = (*env)->NewObject(env,infoObj_class,infoObjConstructor,objName);
   if( (*env)->ExceptionOccurred(env) != NULL) return NULL;

   (*env)->SetObjectArrayElement(env,objs,i,obj);
   exc = (*env)->ExceptionOccurred(env);
   if (exc) {
      jthrowable pbsException;
      (*env)->ExceptionClear(env);
      pbsException = (*env)->NewObject(env,pbsException_class,pbsExceptionConstructor,exc);
      (*env)->Throw(env,pbsException); 
      return NULL;
   }
   /* get PBSInfoObject.attrs.put() */
   attrsMap = (*env)->GetObjectField(env, obj, attrsId);
   if( (*env)->ExceptionOccurred(env) != NULL) return NULL;
   if(attrsMap==NULL) {
       jmethodID exceptionConstructor = (*env)->GetMethodID(env, pbsException_class, "<init>", "(Ljava/lang/String;)V");
       jstring text = (*env)->NewStringUTF(env,"attrsMap is null");
       jthrowable exception = (*env)->NewObject(env,pbsException_class,exceptionConstructor,text);
       (*env)->Throw(env,exception);
       return NULL;
   }
   attrsClass = (*env)->GetObjectClass(env,attrsMap);
   if( (*env)->ExceptionOccurred(env) != NULL) return NULL;
   putId = (*env)->GetMethodID(env, attrsClass, "put", "(Ljava/lang/Object;Ljava/lang/Object;)Ljava/lang/Object;");
   if (putId == 0) { return NULL; }


   /* fill Map of attributes */
   for(atp=tmp->attribs;atp!=NULL;atp=atp->next) {
     jstring name,value;
     if(atp->resource!=NULL) {
      char buff[256];
      sprintf(buff,"%s.%s",atp->name,atp->resource);
      name = (*env)->NewStringUTF(env, buff);
     } else {
      name = (*env)->NewStringUTF(env, atp->name);
     }
     value = (*env)->NewStringUTF(env, atp->value);
     /*intern name string*/
     //name = (*env)->CallObjectMethod(env,name,internId);
     /*put to attribute map*/
     (*env)->CallObjectMethod(env,attrsMap,putId,name,value);
     if( (*env)->ExceptionOccurred(env) != NULL) return NULL;
   }
 }
 /* free allocated memory */
 pbs_statfree(bs);
 /* return */
 return objs;
}

