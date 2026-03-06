# TP CAPSTONE - Application de Réservation de Salles

##  Contexte
Ce projet est un TP Capstone démontrant l'utilisation avancée de **JPA/Hibernate** avec :
- Optimistic Locking (@Version)
- Cache de second niveau (EhCache)
- Requêtes complexes et pagination
- Tests de performance

---



### OPTION 1 : Initialisation des données de test
<img width="640" height="563" alt="initialisation-donnée " src="https://github.com/user-attachments/assets/81803c32-0191-406b-aaac-d00e45ad717d" />


---

### OPTION 2 : Scénarios de test

#### TEST 1 - Recherche de disponibilité**


<img width="643" height="575" alt="scenarios-test1" src="https://github.com/user-attachments/assets/619e2ff4-dad7-4dcc-82b7-0fa29881783f" />


#### TEST 2 - Recherche multi-critères

<img width="644" height="398" alt="scenarios-test2" src="https://github.com/user-attachments/assets/0bf1399b-cedb-408e-95cd-19f61aa7fc30" />


#### **TEST 3 - Pagination**

<img width="642" height="589" alt="scenarios-test3" src="https://github.com/user-attachments/assets/9bfb622c-2cc4-48b1-8d37-a07e82cd1aa4" />


#### **TEST 4 - OPTIMISTIC LOCKING** 

<img width="640" height="573" alt="scenarios-test4 1" src="https://github.com/user-attachments/assets/0b9b2696-f627-4bbe-83b9-426ff99004db" />

<img width="645" height="203" alt="scenarios-test4 2" src="https://github.com/user-attachments/assets/514c7820-530e-45ac-8dbc-30e94273130c" />

✅ *Le deuxième thread échoue car la version a changé*


#### **TEST 5 - Performance du cache**

*Comparaison avec/sans cache de second niveau*

<img width="629" height="152" alt="scenarios-test5-avecCache" src="https://github.com/user-attachments/assets/896b519a-ed60-4445-ac38-20fc195a531a" />

<img width="650" height="88" alt="scenarios-test5-sanscache" src="https://github.com/user-attachments/assets/128c6ad4-6bb9-4ecf-8e91-0f510f2000ef" />


---

### **OPTION 3 : Script de migration**

*Exécution du script de migration sur H2*

<img width="641" height="579" alt="migration" src="https://github.com/user-attachments/assets/5e872992-ca6f-4f7b-a8f7-416e6f481909" />

---

### **OPTION 4 : Rapport de performance**

*Génération du rapport de performance*

<img width="648" height="177" alt="rapport1" src="https://github.com/user-attachments/assets/1b061fa3-697e-4478-b8e3-cccc4a0ca694" />

<img width="633" height="91" alt="rapport2" src="https://github.com/user-attachments/assets/37f9d668-1324-43e6-ade7-7ad41ae46062" />

<img width="637" height="144" alt="rapport3" src="https://github.com/user-attachments/assets/3d7921b8-f616-4d81-9176-c0a9ff81d0c4" />



---

### **OPTION 5 : Statistiques**

<img width="636" height="197" alt="statistique" src="https://github.com/user-attachments/assets/5954f317-c0f7-4f38-8f45-54d964cd133e" />






