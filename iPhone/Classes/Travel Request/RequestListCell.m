//
//  RequestListCell.m
//  ConcurMobile
//
//  Created by Laurent Mery on 8/21/14.
//  Copyright (c) 2014 Concur. All rights reserved.
//

#import "RequestListCell.h"

#import "CTETravelRequest.h"

#import "UIView+Styles.h"
#import "UIColor+ConcurColor.h"


@interface RequestListCell ()

@property (weak, nonatomic) IBOutlet UILabel  *requestName;
@property (weak, nonatomic) IBOutlet UILabel  *segmentTypes;
@property (weak, nonatomic) IBOutlet UILabel  *approvalStatus;
@property (weak, nonatomic) IBOutlet UILabel  *requestTotal;
@property (weak, nonatomic) IBOutlet UILabel  *startDate;

@property (weak, nonatomic) IBOutlet NSLayoutConstraint *constraintWidthApprovalStatus;
@property (weak, nonatomic) IBOutlet NSLayoutConstraint *constrainteWidthAmount;

@property (weak, nonatomic) CTETravelRequest *request;

@end

@implementation RequestListCell

//public
-(CTETravelRequest*)getRequest{
	
	return _request;
}

//public
-(void)updateCellWithRequestDatas:(CTETravelRequest *)request{
	
	
	_request = request;
	    
    //set datas
    [_requestName setText:[request.Name stringValue]];
    [_segmentTypes setText: [request.SegmentTypes stringValue]];
    [_approvalStatus setText: [request.ApprovalStatusName stringValue]];
    [_requestTotal setText: [request.TotalPostedAmount stringValue]];
	
    [_startDate setText: [request.StartDate stringValue]];
    
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
