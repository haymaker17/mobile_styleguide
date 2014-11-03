//
//  DetailApproverList.h
//  ConcurMobile
//
//  Created by Paul Kramer on 5/29/10.
//  Copyright 2010 Concur. All rights reserved.
//

#import <UIKit/UIKit.h>
#import "EntryData.h"
#import "ReportData.h"

@protocol DetailApprovalListDelegate
- (void)cancelApprovalList;
@end

@interface DetailApproverList : UIViewController {
	UITableView			*tableList;
	int					listType;
	id<DetailApprovalListDelegate> __weak _delegate;
	NSMutableArray		*aRows, *aSections, *aHeaders;
	EntryData			*entry;
	ReportData			*report;
	float				adjustedHeight;
	UIToolbar			*tBar;
	BOOL				isSummary;
	float				labelFontSize, valueFontSize;
}

@property float				adjustedHeight;
@property float				labelFontSize;
@property float				valueFontSize;
@property (nonatomic, weak) id<DetailApprovalListDelegate> delegate;
@property int			listType;
@property (nonatomic, strong) IBOutlet UITableView			*tableList;
@property (nonatomic, strong) IBOutlet UIToolbar			*tBar;

@property (nonatomic, strong) NSMutableArray		*aRows;
@property (nonatomic, strong) NSMutableArray		*aSections;
@property (nonatomic, strong) NSMutableArray		*aHeaders;
@property (nonatomic, strong) EntryData				*entry;
@property (nonatomic, strong) ReportData			*report;
@property BOOL isSummary;

-(void) makeEntryDetails:(EntryData *) e;
-(NSArray *) makeAttendeeDetails:(EntryData *) e;
-(void) makeCommentDetails:(EntryData *) e;
-(void) makeExceptionDetails:(EntryData *) e;
-(void) makeItemizationDetails:(EntryData *) e;

-(void) makeCommentDetailsReport:(ReportData *) r;

-(void) setToolBarTitle:(NSString *)viewTitle;

-(void) makeSummaryReport:(ReportData *) r;
-(void) makeExceptionDetailsReport:(ReportData *) r;
-(void)resizePopover;

@end
