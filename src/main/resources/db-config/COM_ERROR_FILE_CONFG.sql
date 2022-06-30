CREATE TABLE COM_ERROR_FILE_CONFG(
	ERROR_FILE_CONFIG_ID NUMBER,
	IM_CODE VARCHAR(20),
	FILE_STRUCT VARCHAR(20),
	FILE_DLMTR VARCHAR(10),
	FILE_ID VARCHAR(10),
	REFERENCE_NO VARCHAR(10),
	CREATION_TIME VARCHAR(10),
	INVC_NMBR VARCHAR(10),
	INVC_AMNT VARCHAR(10),
	INVC_DATE VARCHAR(10),
	REVERSAL_DATE VARCHAR(10),
	VNDR_CODE VARCHAR(10),
	VNDR_NAME VARCHAR(10),
	STATUS VARCHAR(10),
	STATUS_DESC VARCHAR(10),
	ECHEQUE_NO VARCHAR(10),
	ADDITIONAL_FIELD_1 VARCHAR(50),
	ADDITIONAL_FIELD_2 VARCHAR(50),
	ADDITIONAL_FIELD_3 VARCHAR(50),
	ADDITIONAL_FIELD_4 VARCHAR(50),
	ADDITIONAL_FIELD_5 VARCHAR(50),
	ADDITIONAL_FIELD_6 VARCHAR(50),
	ADDITIONAL_FIELD_7 VARCHAR(50),
	ADDITIONAL_FIELD_8 VARCHAR(50),
	ADDITIONAL_FIELD_9 VARCHAR(50),
	ADDITIONAL_FIELD_10 VARCHAR(50),
	CREATED TIMESTAMP,
	CREATED_BY VARCHAR(100),
	UPDATED TIMESTAMP,
	UPDATED_BY VARCHAR(100)
);

CREATE INDEX "COM_ERROR_FILE_CONFG_IDX" ON "COM_ERROR_FILE_CONFG" ("IM_CODE");

ALTER TABLE "COM_ERROR_FILE_CONFG" ADD CONSTRAINT "COM_ERROR_FILE_CONFG" PRIMARY KEY ("ERROR_FILE_CONFIG_ID") ENABLE;

ALTER TABLE "COM_ERROR_FILE_CONFG" MODIFY ("IM_CODE" NOT NULL ENABLE);
ALTER TABLE "COM_ERROR_FILE_CONFG" MODIFY ("FILE_STRUCT" NOT NULL ENABLE);
ALTER TABLE "COM_ERROR_FILE_CONFG" MODIFY ("FILE_DLMTR" NOT NULL ENABLE);
ALTER TABLE "COM_ERROR_FILE_CONFG" MODIFY ("FILE_ID" NOT NULL ENABLE);
ALTER TABLE "COM_ERROR_FILE_CONFG" MODIFY ("REFERENCE_NO" NOT NULL ENABLE);
ALTER TABLE "COM_ERROR_FILE_CONFG" MODIFY ("CREATION_TIME" NOT NULL ENABLE);
ALTER TABLE "COM_ERROR_FILE_CONFG" MODIFY ("INVC_NMBR" NOT NULL ENABLE);
ALTER TABLE "COM_ERROR_FILE_CONFG" MODIFY ("INVC_AMNT" NOT NULL ENABLE);
ALTER TABLE "COM_ERROR_FILE_CONFG" MODIFY ("INVC_DATE" NOT NULL ENABLE);
ALTER TABLE "COM_ERROR_FILE_CONFG" MODIFY ("REVERSAL_DATE" NOT NULL ENABLE);
ALTER TABLE "COM_ERROR_FILE_CONFG" MODIFY ("VNDR_CODE" NOT NULL ENABLE);
ALTER TABLE "COM_ERROR_FILE_CONFG" MODIFY ("VNDR_NAME" NOT NULL ENABLE);
ALTER TABLE "COM_ERROR_FILE_CONFG" MODIFY ("STATUS" NOT NULL ENABLE);
ALTER TABLE "COM_ERROR_FILE_CONFG" MODIFY ("STATUS_DESC" NOT NULL ENABLE);
ALTER TABLE "COM_ERROR_FILE_CONFG" MODIFY ("ECHEQUE_NO" NOT NULL ENABLE);
ALTER TABLE "COM_ERROR_FILE_CONFG" MODIFY ("CREATED" NOT NULL ENABLE);
ALTER TABLE "COM_ERROR_FILE_CONFG" MODIFY ("CREATED_BY" NOT NULL ENABLE);
ALTER TABLE "COM_ERROR_FILE_CONFG" MODIFY ("UPDATED" NOT NULL ENABLE);
ALTER TABLE "COM_ERROR_FILE_CONFG" MODIFY ("UPDATED_BY" NOT NULL ENABLE);
