SET FOREIGN_KEY_CHECKS=0;
delete from item where id > 0;
delete FROM itembatch WHERE `ID`> 0;
delete from stock  WHERE `ID` > 0;
delete from stockhistory WHERE `ID` > 0;
delete from userstock  WHERE `ID` > 0;
delete from userstockcontainer WHERE `ID` > 0;
SET FOREIGN_KEY_CHECKS=1;

