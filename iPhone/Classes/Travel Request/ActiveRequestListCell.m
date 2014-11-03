//
//  ActiveRequestListCell.m
//  ConcurMobile
//
//  Created by Laurent Mery on 8/21/14.
//  Copyright (c) 2014 Concur. All rights reserved.
//

#import "ActiveRequestListCell.h"

#import "CTETravelRequest.h"

#import "UIView+Styles.h"
#import "CCFormatUtilities.h"
#import "UIColor+ConcurColor.h"


@interface ActiveRequestListCell ()

@property (weak, nonatomic) IBOutlet UILabel  *requestName;
@property (weak, nonatomic) IBOutlet UILabel  *segmentTypes;
@property (weak, nonatomic) IBOutlet UILabel  *approvalStatus;
@property (weak, nonatomic) IBOutlet UILabel  *requestTotal;
@property (weak, nonatomic) IBOutlet UILabel  *startDate;

@property (weak, nonatomic) IBOutlet NSLayoutConstraint *constraintWidthApprovalStatus;
@property (weak, nonatomic) IBOutlet NSLayoutConstraint *constrainteWidthAmount;

@property (weak, nonatomic) CTETravelRequest *request;

@end

@implementation ActiveRequestListCell

//public
-(CTETravelRequest*)getRequest{
	
	return _request;
}

//public
-(void)updateCellWithRequestDatas:(CTETravelRequest *)request{
	
	
	_request = request;
	
    //convert Amount to localized
	NSString *requestAmount = [CCFormatUtilities formatAmount:request.TotalPostedAmount withCurrency:request.CurrencyCode localisedOrNil:nil];
    
    //convert date
    NSString *startDate = [CCFormatUtilities formatedDateYYYYMMddTHHmmss:request.StartDate withTemplate:@"eeedM"];
    
    //set datas
    [_requestName setText:request.Name];
    [_segmentTypes setText: request.SegmentTypes];
    [_approvalStatus setText: request.ApprovalStatusName];
    [_requestTotal setText: requestAmount];
    [_startDate setText: startDate];
    
    //set Concur color
    [_segmentTypes setTextColor:[UIColor textLightTitle]];
    [_requestTotal setTextColor:[UIColor textAmountInList]];
    [_startDate setTextColor:[UIColor textLightTitle]];
    [_approvalStatus setTextColor:[UIColor textWorkflowStatusInList]];
    
    //design a border on apsKey
	[_approvalStatus applyStyleForDisplayApprovalStatusBorderedByConstrainte:_constraintWidthApprovalStatus withMaxWidth:150];
	
	//adjust width amount to its content and push segment by constrainte
	[_requestTotal applyFitContentByConstrainte:_constrainteWidthAmount withMaxWidth:150 andMarge:0];
}

@end
