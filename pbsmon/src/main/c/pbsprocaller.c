#include <stdio.h>
#include <stdlib.h>
#include <pbs_error.h>
#include <pbs_ifl.h>
#include <unistd.h>
#include <signal.h>

extern char *pbs_server;

int main(int argc, char **argv) {
    /*PBS variables*/
    char *errmsg;
    int con;
    int res;
    struct batch_status *bs;
    /* my variables */
    char* server;

    if(argc != 2) {
        fprintf(stderr,"Usage: %s servername\n",argv[0]);
        return 1;
    }
    server = argv[1];
    
    con = pbs_connect(server);    
    if(con<0) {
        fprintf(stderr,"Cannot connect to %s, error %d \n",server,pbs_errno);
        return 1;
    }
    /* get server info */
    bs = pbs_statserver(con, NULL, NULL);
    process_data(bs,"servers");
    /* get queues info */
    bs = pbs_statque(con, "", NULL, NULL);
    process_data(bs,"queues");
    /* get nodes info */
    bs = pbs_statnode(con, "", NULL, NULL);
    process_data(bs,"nodes");
    /* get jobs info: t - job arrays, x - finished jobs*/
    bs = pbs_statjob(con, "", NULL, "tx");
    process_data(bs,"jobs");
    /* end connection */
    pbs_disconnect(con);
    return 0;
}

int process_data(struct batch_status *bs,char* type) {
    struct batch_status *tmp;
    struct attrl *atp;
    int i;
    printf("\x1E%s\n",type);
    for(tmp=bs,i=0;tmp!=NULL;tmp=tmp->next,i++) {
        printf("\x1F%s\n",tmp->name);

        for(atp=tmp->attribs;atp!=NULL;atp=atp->next) {
            if(atp->resource!=NULL) {
                char buff[256];
                sprintf(buff,"%s.%s",atp->name,atp->resource);
                printf("%s=%s\n",buff,atp->value);                
            } else {
                printf("%s=%s\n",atp->name,atp->value);                
            }
        }
    }
    /* free allocated memory */
    pbs_statfree(bs);
}
