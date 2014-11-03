//
//  ActiveRequestDigestEntryCell.m
//  ConcurMobile
//
//  Created by laurent mery on 26/09/2014.
//  Copyright (c) 2014 Concur. All rights reserved.
//

#import "ActiveRequestDigestEntryCell.h"

#import "CTETravelRequestEntry.h"
#import "CTETravelRequestDigestCellInfos.h"

#import "UIView+Styles.h"
#import "CCFormatUtilities.h"
#import "UIColor+ConcurColor.h"
#import "NSStringAdditions.h"

@interface ActiveRequestDigestEntryCell()

@property (weak, nonatomic) IBOutlet UIImageView *imageIconType;

@property (weak, nonatomic) IBOutlet UILabel *labelEntryName;
@property (weak, nonatomic) IBOutlet UILabel *labelLocations;
@property (weak, nonatomic) IBOutlet NSLayoutConstraint *constrainteLeadingTopEntryName;

@property (weak, nonatomic) IBOutlet UILabel *labelAmount;
@property (weak, nonatomic) IBOutlet NSLayoutConstraint *constrainteWidthAmount;
@property (weak, nonatomic) IBOutlet UILabel *labelDate;
@property (weak, nonatomic) IBOutlet NSLayoutConstraint *constrainteWidthDate;
@property (weak, nonatomic) IBOutlet NSLayoutConstraint *constrainteLeadingTopAmount;

@end

@implementation ActiveRequestDigestEntryCell


- (void)awakeFromNib {
    // Initialization code
}


- (void)setSelected:(BOOL)selected animated:(BOOL)animated {
    [super setSelected:selected animated:animated];

    // Configure the view for the selected state
}

//public
-(void)updateCellWithEntry:(CTETravelRequestEntry *)entry{
	
	CTETravelRequestDigestCellInfos *cellInfos = [entry getDigestCellInfos];
	
	//convert Amount to localized
	NSString *amount = [CCFormatUtilities formatAmount:cellInfos.Amount withCurrency:cellInfos.CurrencyCode localisedOrNil:nil];
	
	//convert date
	NSString *date = [CCFormatUtilities formatedDateMdyyyy:cellInfos.EarlyDate withTemplate:@"eeedM"];
	
	//format Location
	NSString *location = @"";
	if (cellInfos.Location2 == nil) {
		
		location = cellInfos.Location1;
	}
	else {
	
		location = [NSString stringWithFormat:@"%@ %@ %@", cellInfos.Location1, [@"to" localize], cellInfos.Location2];
	}
	
	//set Datas
	[_labelEntryName setText:cellInfos.SegmentName];
	[_labelAmount setText:amount];
	[_labelDate setText:date];
	[_labelLocations setText:location];
	[_imageIconType setImage:[UIImage imageNamed:[NSString stringWithFormat:@"icongray-st_%@", cellInfos.Icon]]];
	
	//set Concur color
	[_labelEntryName setTextColor:[UIColor blackConcur]];
	[_labelAmount setTextColor:[UIColor textAmountInList]];
	[_labelLocations setTextColor:[UIColor textSubTitle]];
	[_labelDate setTextColor:[UIColor textSubTitle]];
	
	
	
	[self fitAmountToContentWidthMaxWidth:120];
}

//public
-(void)updateCellWithTotal:(NSString*)total endCurrencyCode:(NSString*)currencyCode{
	
	//localized amount (ordered currency/amount)
	NSString *amount = [CCFormatUtilities formatAmount:total withCurrency:currencyCode localisedOrNil:nil];
	
	[_labelEntryName setText:[[@"Total" localize] stringByAppendingString:@":"]];
	[_labelAmount setText:amount];
	
	//set Concur color
	[_labelEntryName setTextColor:[UIColor blackConcur]];
	[_labelAmount setTextColor:[UIColor textAmountInList]];
	
	//change font size
	_labelAmount.font = [UIFont fontWithName:@"HelveticaNeue-Medium" size:20.0];
	
	//hide elements in total row
	[_imageIconType setHidden:YES];
	[_labelLocations setHidden:YES];
	[_labelDate setHidden:YES];
	
	//center Total Label (entryName) and total amount (amount)
	[_constrainteLeadingTopEntryName setConstant:15];
	[_labelEntryName needsUpdateConstraints];
	
	[_constrainteLeadingTopAmount setConstant:11]; // amount is biggest than total label
	[_labelAmount needsUpdateConstraints];
	
	
	//display a bottom border
	[self.contentView setBorders:@"b" WithColor:[UIColor borderViewToHighlightWhiteSubview] andBorderWidth:1];
	
	/*
	 * adjust startDate width size depending on its content and leave max place to request name if no startDate
	 * we use many constraints to do the job
	 */
	[self fitAmountToContentWidthMaxWidth:135];
}


-(void)fitAmountToContentWidthMaxWidth:(CGFloat)maxWidth{

	[_labelAmount applyFitContentByConstrainte:_constrainteWidthAmount withMaxWidth:maxWidth andMarge:8];
}

@end
