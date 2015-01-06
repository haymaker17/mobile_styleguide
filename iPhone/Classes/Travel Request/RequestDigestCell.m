//
//  RequestDigestCell.m
//  ConcurMobile
//
//  Created by laurent mery on 26/09/2014.
//  Copyright (c) 2014 Concur. All rights reserved.
//

#import "RequestDigestCell.h"

#import "CTETravelRequestEntry.h"
#import "CTETravelRequestDigestCellInfos.h"

#import "UIView+Styles.h"
#import "UIColor+ConcurColor.h"
#import "NSStringAdditions.h"
#import "CTEDataTypes.h"

@interface RequestDigestCell()

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

@implementation RequestDigestCell


- (void)awakeFromNib {
    // Initialization code
}


- (void)setSelected:(BOOL)selected animated:(BOOL)animated {
    [super setSelected:selected animated:animated];

    // Configure the view for the selected state
}

//public
-(void)updateCellWithEntry:(CTETravelRequestEntry *)entry{
	
	_isTotalRow = NO;
	
	CTETravelRequestDigestCellInfos *cellInfos = [entry getDigestCellInfos];
	
	//format Location
	NSString *location = @"";
	if ([cellInfos.Location2 isEmpty]) {
		
		location = [cellInfos.Location1 stringValue];
	}
	else {
	
		location = [NSString stringWithFormat:@"%@ %@ %@", [cellInfos.Location1 stringValue], [@"to" localize], [cellInfos.Location2 stringValue]];
	}
	
	//set Datas //
	[_labelEntryName setText:[cellInfos.SegmentName stringValue]];
	[_labelAmount setText:[cellInfos.Amount stringValue]];
	
	[cellInfos.EarlyDate setDateOutputTemplate:@"EEEdM"];
	[_labelDate setText:[cellInfos.EarlyDate stringValue]];
	
	[_labelLocations setText:location];
	[_imageIconType setImage:[UIImage imageNamed:[NSString stringWithFormat:@"icongray-st_%@", [cellInfos.Icon stringValue]]]];
	
	//set Concur color
	[_labelEntryName setTextColor:[UIColor blackConcur]];
	[_labelAmount setTextColor:[UIColor textAmountInList]];
	[_labelLocations setTextColor:[UIColor textSubTitle]];
	[_labelDate setTextColor:[UIColor textSubTitle]];
	
	//adjust the bottom separator
	[self setSeparatorInset:UIEdgeInsetsMake(0.f, 40.f, 0.f, 0.f)];
	
	
	[self fitAmountToContentWidthMaxWidth:125];
}

//public
-(void)updateCellWithTotal:(NSString*)total{
	
	_isTotalRow = YES;

	[_labelEntryName setText:[[@"Total" localize] stringByAppendingString:@":"]];
	[_labelAmount setText:total];
	
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

	
	/*
	 * adjust startDate width size depending on its content and leave max place to request name if no startDate
	 * we use many constraints to do the job
	 */
	[self fitAmountToContentWidthMaxWidth:220];
}


-(void)fitAmountToContentWidthMaxWidth:(CGFloat)maxWidth{

	[_labelAmount applyFitContentByConstrainte:_constrainteWidthAmount withMaxWidth:maxWidth andMarge:8];
}

@end
