ALTER TABLE pharmaceuticalbillitem
    ADD COLUMN wholesaleFreeFor double  NULL,
    ADD COLUMN wholesaleFreeQty double  NULL;

ALTER TABLE itembatch
    ADD COLUMN wholesaleFreeFor double  NULL,
    ADD COLUMN wholesaleFreeQty double  NULL;

