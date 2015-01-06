//
//  FFCells.h
//  ConcurMobile
//
//  Created by laurent mery on 25/11/2014.
//  Copyright (c) 2014 Concur. All rights reserved.
//

#import <UIKit/UIKit.h>

@class FFField, CTEDataTypes;




extern NSString *const FFCellReuseIdentifierStatic;
extern NSString *const FFCellReuseIdentifierText;
extern NSString *const FFCellReuseIdentifierTextArea;
extern NSString *const FFCellReuseIdentifierDate;
extern NSString *const FFCellReuseIdentifierNumber;
extern NSString *const FFCellReuseIdentifierMoney;




@interface FFBaseCell : UITableViewCell

@property (nonatomic, retain) FFField *field;

//TODO: instead keeping indexpath, be able on ffformController.m side to determince tableview.cell with self
@property (nonatomic, retain) NSIndexPath *indexPath;


-(void)initWithField:(FFField*)field andDelegate:(id)delegate;
-(CGFloat)heightView;

@end





@interface FFStaticCell : FFBaseCell
@end





@interface FFTextCell : FFBaseCell
@end





@interface FFTextAreaCell : FFStaticCell
@end




@interface FFDateCell : FFTextCell
@end





@interface FFNumberCell : FFTextCell
@end





@interface FFMoneyCell : FFNumberCell
@end