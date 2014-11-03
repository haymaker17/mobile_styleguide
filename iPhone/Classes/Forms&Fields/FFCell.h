//
//  FFCell.h
//  ConcurMobile
//
//  Created by laurent mery on 29/10/2014.
//  Copyright (c) 2014 Concur. All rights reserved.
//

#import <UIKit/UIKit.h>
@class CTEField;

@interface FFCell : UITableViewCell

@property (nonatomic, assign) CGFloat labelBoxLeft;
@property (nonatomic, assign) CGFloat labelFormLabelTop;
@property (nonatomic, assign) CGFloat labelFormValueTop;
@property (nonatomic, assign) CGFloat labelBoxWidth;
@property (nonatomic, assign) CGFloat labelBoxHeight;
@property (nonatomic, assign) CGFloat cellWidthMax;


@property (nonatomic, strong) UILabel *formLabel;
@property (nonatomic, strong) UILabel *formValue;

-(void)setDisclosureIndicatorHidden:(BOOL)hidden;

-(void)setLabel:(NSString*)value;
-(void)setValue:(NSString*)value;

-(void)markInvalid;
-(void)clearInvalid;

-(CGFloat)heightView;

@end



@interface FFCellMultiLine : FFCell

@property (nonatomic, assign) CGFloat margeBottomForMultiLine;

@end