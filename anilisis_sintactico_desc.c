// Tarea 2
//Analisis sintacitco escendende recursivo
#include "anlex.c"
#include <stdio.h>
#include <stdlib.h>
#include <ctype.h>
#include <string.h>
#define CANT_COMP_LEX 10

void json();
void element();
void array();
void a_();
void element_list();
void elemen_();
void object();
void o_();
void attributes_list();
void at_list_();
void attribute();
void attribute_name();
void attribute_value(int []);
void match(int);//machear los resultados
void error(int []);//pop o scan
int accept=1;//valor de aceptacion 

void match(int token){
    if(t.compLex == token){
        sigLex();
    }  
}

void error(int sincronizacion []){
    accept=0;
   	printf("\nError sintactico en linea: %d. No se esperaba %s.\n",numLinea,t.componente);
	int i = 0;
    while(t.compLex != sincronizacion[i] && t.compLex != EOF){   
        if (sincronizacion[i] == '\0')
        {
            sigLex();
            i=0;        
        }
        i++;
    }
    sigLex();
    return;
}

void check_input(int primero[], int siguiente[]){
    int syncset[CANT_COMP_LEX];
    int i=0;
    if(t.compLex == EOF) return;    
    while(primero[i] != '\0') {
        if(t.compLex == primero[i]){
            return;
        }
        i++;
    }
    i=0;    
    while(primero[i] != '\0'){ 
        syncset[i] = primero[i];
        i++;
    }
    int j=0;
    while(siguiente[j] != '\0'){
        syncset[i] = siguiente[j];
        i++;
        j++;
    }
    error_sint(syncset);
}

 
void json(){
    int primero[] = {L_CORCHETE,L_LLAVE,'\0'};
    int siguiente[] = {COMA,R_CORCHETE,R_LLAVE, '\0'};
    element(siguiente);
}

void element(int syncset[]){

    int primero[] = {L_CORCHETE,L_LLAVE,'\0'};
    int siguiente[] = {COMA,R_CORCHETE,R_LLAVE, '\0'};
    check_input(primero,syncset);

    if(t.compLex == L_LLAVE){
        object(siguiente);
    }
    else if(t.compLex == L_CORCHETE){
        array(siguiente);
    }
    check_input(siguiente,primero);
}

void array(int syncset[]){
    int primero[] = {L_CORCHETE,'\0'};
    int siguiente[] = {COMA,R_CORCHETE,R_LLAVE, '\0'};
    check_input(primero,syncset);
   
    if(t.compLex == L_CORCHETE){
        match(L_CORCHETE);
        arrayB(siguiente);
    }
    check_input(siguiente,primero);
}

void a_(int syncset[]){
    int primero[]={L_CORCHETE,R_CORCHETE,L_LLAVE,'\0'};
    int siguiente[] = {L_CORCHETE,COMA,R_CORCHETE,R_LLAVE, '\0'};
    check_input(primero,syncset);
    if(t.compLex == L_CORCHETE || t.compLex == L_LLAVE){
        element_list(siguiente);
        match(R_CORCHETE);
    }
    else if(t.compLex == R_CORCHETE){
        match(R_CORCHETE);
    }
    check_input(siguiente,primero);

}

void element_list(int syncset[]){   
    int primero[]={L_CORCHETE,L_LLAVE,'\0'};
    int siguiente[] = {R_CORCHETE, '\0'};
    check_input(primero,syncset);
    if(t.compLex == L_CORCHETE || t.compLex == L_LLAVE){
        element(siguiente);
        element_listB(siguiente);
    }
    check_input(siguiente,primero);
}

void elemen_ (int syncset[]){
    if(t.compLex == R_CORCHETE){ 
        return;
    }
    int primero[]={COMA,'\0'};
    int siguiente[] = {R_CORCHETE, '\0'};
    check_input(primero,syncset);
    if(t.compLex == COMA){
        match(COMA);
        element(siguiente);
        element_listB(siguiente);
    }
    
    check_input(siguiente,primero);
}

void object(int syncset[]){
	int primero[]={L_LLAVE,'\0'};
    int siguiente[] = {COMA,R_CORCHETE,R_LLAVE, '\0'};
    check_input(primero,syncset);
    if(t.compLex == L_LLAVE){
        match(L_LLAVE);
        objectB(siguiente);
    }
    check_input(siguiente,primero);

}

void o_(int syncset[]){
    int primero[]={LITERAL_CADENA,R_LLAVE,'\0'};
    int siguiente[] = {LITERAL_CADENA,COMA,R_CORCHETE,R_LLAVE, '\0'};
    check_input(primero,syncset);
    if(t.compLex == LITERAL_CADENA){
        attributes_list(siguiente);
        match(R_LLAVE);
    }
    else if(t.compLex == R_LLAVE){
        match(R_LLAVE);
    }
    check_input(siguiente,primero);
}

void attributes_list(int syncset[]){
    int primero[]={LITERAL_CADENA,'\0'};
    int siguiente[] = {R_LLAVE, '\0'};
    check_input(primero,syncset);

    if(t.compLex == LITERAL_CADENA){
        attribute(siguiente);
        attributes_listB(siguiente);
    }
    check_input(siguiente,primero);
}

void at_list_(int syncset[]){
 
    if (t.compLex == R_LLAVE){
        return;
    }
    int primero[]={COMA,'\0'};
    int siguiente[] = {R_LLAVE, '\0'};
    check_input(primero,syncset);
    if(t.compLex == COMA){
        match(COMA);
        attribute(siguiente);
        attributes_listB(siguiente);
    }
    
    check_input(siguiente,primero);
}

void attribute(int syncset[]){
    int primero[]={LITERAL_CADENA,'\0'};
    int siguiente[] = {COMA,R_LLAVE, '\0'};
    check_input(primero,siguiente);
    if(t.compLex == LITERAL_CADENA){
        attribute_name(siguiente);
        match(DOS_PUNTOS);
        attribute_value(siguiente);
    }
    check_input(siguiente,primero);
}

void attribute_name(int syncset[]){
    int primero[]={LITERAL_CADENA,'\0'};
    int siguiente[] = {DOS_PUNTOS, '\0'};
    check_input(primero,syncset);
    if(t.compLex == LITERAL_CADENA){
        match(LITERAL_CADENA);
    }
    check_input(siguiente,primero);
}

void attribute_value(int syncset[]){
    int primero[]={L_CORCHETE,L_LLAVE,LITERAL_CADENA,LITERAL_NUM,PR_TRUE,PR_FALSE,PR_NULL,'\0'};
    int siguiente[] = {COMA,R_LLAVE, '\0'};
    check_input(primero,syncset);
    if(t.compLex == L_CORCHETE || t.compLex == L_LLAVE){
        element(siguiente);
    }
    else if(t.compLex == LITERAL_CADENA){
        match(LITERAL_CADENA);
    }
    else if(t.compLex == LITERAL_NUM){
        match(LITERAL_NUM);
    }
    else if(t.compLex == PR_TRUE){
        match(PR_TRUE);
    }
    else if(t.compLex == PR_FALSE){
        match(PR_FALSE);
    }
    else if(t.compLex == PR_NULL){
        match(PR_NULL);
    }
    check_input(siguiente,primero);

}





















