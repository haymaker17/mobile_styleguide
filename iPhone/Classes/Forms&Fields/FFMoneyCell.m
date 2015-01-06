//
//  FFMoneyCell.m
//  ConcurMobile
//
//  Created by Laurent Mery on 09/12/14.
//  Copyright (c) 2014 Concur. All rights reserved.
//

#import "FFCells-private.h"

@implementation FFMoneyCell


#pragma mark - init

NSString *const FFCellReuseIdentifierMoney = @"MoneyCell";



- (instancetype)initWithStyle:(UITableViewCellStyle)style reuseIdentifier:(NSString *)reuseIdentifier{
    
    if (self = [super initWithStyle:style
                    reuseIdentifier:reuseIdentifier]){
        
        
        [self setVAlignCenterIfEmpty:NO];

    }
    
    return self;
}


#pragma mark - Layout

-(void)render{
    
    [super render];
    
    CGFloat widthLineSeparator = 1;
    
    CGFloat viewAmountWidth = (self.contentView.frame.size.width / 2) + widthLineSeparator;
    CGFloat viewCurrencyWidth =  self.contentView.frame.size.width - viewAmountWidth;
    CGFloat viewHeight = 63.0; //TODO: caclcul correct height (contentView.frame.size.height return 44 instead 63
    
    _viewAmount = [[UIView alloc] initWithFrame:CGRectMake(0, 0, viewAmountWidth, viewHeight)];
    _viewCurrency = [[UIView alloc] initWithFrame:CGRectMake(viewAmountWidth, 0, viewCurrencyWidth, viewHeight)];
    
    
    //add line separator
    UIColor *colorLineSeparator = [UIColor borderFormCellLineSeparator];
    CALayer *lineSeparator = [CALayer layer];
    lineSeparator.borderColor = colorLineSeparator.CGColor;
    lineSeparator.borderWidth = widthLineSeparator;
    lineSeparator.frame = CGRectMake(viewAmountWidth - widthLineSeparator, 25.0, widthLineSeparator, viewHeight - 30);
    
    [_viewAmount.layer addSublayer:lineSeparator];
    
    [self.contentView insertSubview:_viewAmount atIndex:0];
    [self.contentView insertSubview:_viewCurrency atIndex:0];
}

-(void)initLayout{
    
    [super initLayout];
    
    NSNumber *margeLeft = [self.constraintsMetrics valueForKey:@"margeLeft"];
    CGFloat margeLeftX2 = [margeLeft doubleValue] * 2;
    
    [self removeCellVisualFormatConstraints:@[@"HValue"]];
    [self addCellVisualFormatConstraints:@{
                                           @"HValue": @[
                                                   [NSLayoutConstraint constraintWithItem:self.inputValue
                                                                                attribute:NSLayoutAttributeCenterX
                                                                                relatedBy:0
                                                                                   toItem:self.viewAmount
                                                                                attribute:NSLayoutAttributeCenterX
                                                                               multiplier:1
                                                                                 constant:0],
                                                   [NSLayoutConstraint constraintWithItem:self.inputValue
                                                                                attribute:NSLayoutAttributeWidth
                                                                                relatedBy:NSLayoutRelationEqual
                                                                                   toItem:self.viewAmount
                                                                                attribute:NSLayoutAttributeWidth
                                                                               multiplier:1
                                                                                 constant:-margeLeftX2]
                                                   ]
                                           }];
}

#pragma mark - Component

//public
-(void)updateDataType{

    [super updateDataType];
    
    [self.inputValue setText:[self.field.dataType.Number stringValue]];
}


@end
