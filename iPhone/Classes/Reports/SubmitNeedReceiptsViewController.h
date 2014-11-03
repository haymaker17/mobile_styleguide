//
//  SubmitNeedReceiptsViewController.h
//  ConcurMobile
//
//  Created by yiwen on 6/21/10.
//  Copyright 2010 Concur. All rights reserved.
//

#import <UIKit/UIKit.h>
#import "MobileViewController.h"
#import "SubmitNeedReceiptsDelegate.h"
#import "ReportData.h"

@interface SubmitNeedReceiptsViewController : MobileViewController  <UITableViewDelegate, UITableViewDataSource> 
{
	UITableView						*entryListView;
	NSArray							*entryList;
	UILabel							*lblSubmitConfirm;
	UILabel							*lblReceiptRequired;
	UILabel							*lblHowToProvide;
	UILabel                         *lblSubmitCustomText;
    UIScrollView                    *scrollView;
    
	id<SubmitNeedReceiptsDelegate>	__weak _delegate;
	UIBarButtonItem					*btnSubmit;
	UIBarButtonItem					*btnCancel;
    ReportData                      *rpt;

}

@property (strong, nonatomic) IBOutlet UITableView		*entryListView;
@property (strong, nonatomic) NSArray					*entryList;
@property (strong, nonatomic) IBOutlet UILabel			*lblSubmitConfirm;
@property (strong, nonatomic) IBOutlet UILabel			*lblReceiptRequired;
@property (strong, nonatomic) IBOutlet UILabel			*lblHowToProvide;
@property (strong, nonatomic) IBOutlet UILabel          *lblSubmitCustomText;
@property (strong, nonatomic) IBOutlet UIScrollView     *scrollView;

@property (weak, nonatomic) id<SubmitNeedReceiptsDelegate>	delegate;
@property (strong, nonatomic) IBOutlet UIBarButtonItem	*btnSubmit;
@property (strong, nonatomic) IBOutlet UIBarButtonItem	*btnCancel;
@property (strong, nonatomic) ReportData                *rpt;

@property (strong, nonatomic) NSString                  *howToProvideMsgType;
-(void)buttonCancelPressed:(id)sender;
-(void)buttonSubmitPressed:(id)sender;

@end
