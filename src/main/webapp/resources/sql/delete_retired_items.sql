SET FOREIGN_KEY_CHECKS=0;
delete from item where retired =true and `DTYPE`='Amp';
SET FOREIGN_KEY_CHECKS=1;

