ALTER TABLE pharmaceuticalbillitem
    ADD COLUMN wholeSaleMargin double  NULL,
    ADD COLUMN retailMargin double  NULL;

ALTER TABLE itembatch
    ADD COLUMN wholeSaleMargin double  NULL,
    ADD COLUMN retailMargin double  NULL;

