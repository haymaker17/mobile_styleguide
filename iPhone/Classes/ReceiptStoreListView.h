//
//  ReceiptStoreListView.h
//  ConcurMobile
//
//  Created by Manasee Kelkar on 2/8/11.
//  Copyright 2011 Concur. All rights reserved.
//

#import <UIKit/UIKit.h>
#import "MobileViewController.h"
#import "ExSystem.h" 

#import "iPadHomeVC.h"
#import "UploadQueueVCDelegate.h"
#import "ReceiptDownloaderDelegate.h"

@interface ReceiptStoreListView : MobileViewController  <UITableViewDelegate, UITableViewDataSource,UnifiedImagePickerDelegate,UIActionSheetDelegate,UploadBannerDelegate,UploadQueueVCDelegate,ReceiptDownloaderDelegate> {
	UITableView					*receiptsTableView;
    float                       tableViewY;
	NSMutableArray				*tblDataSource;
	
	UIView						*flipView;
    float                       flipViewY;
	UIBarButtonItem				*uploadButton;
	
	id							__weak delegate;
	NSUInteger					receiptIndexToDelete;
    BOOL                        disableEditActions;
    NSMutableDictionary         *groupedReceiptsLookup;
    NSMutableArray				*sectionsArray;
    
    NSDate                      *lastUpdatedTimestamp; // Receipt store urls have a timeout of 30 mins
    BOOL                        needsDataRefetch;
    BOOL                        canRefresh;
    BOOL                        isQuickMode;
    BOOL                       bannerAdjusted;
}

@property BOOL                                                  bannerAdjusted;
@property BOOL                                                  isQuickMode;
@property float                                                 flipViewY;
@property float                                                 tableViewY;
@property (nonatomic,strong) IBOutlet UITableView				*receiptsTableView;
@property (nonatomic,strong) NSMutableArray						*tblDataSource;

@property (nonatomic,strong) UIView								*flipView;
@property (nonatomic,strong) UIBarButtonItem					*uploadButton;

@property (nonatomic,weak) id									delegate;
@property (nonatomic,assign) NSUInteger							receiptIndexToDelete;
@property (nonatomic,assign) BOOL                               disableEditActions;

@property (nonatomic,strong)  NSMutableDictionary               *groupedReceiptsLookup;
@property (nonatomic,strong)  NSMutableArray                    *sectionsArray;

@property (nonatomic, strong) NSDate                            *lastUpdatedTimestamp; // Receipt store urls have a timeout of 30 mins
@property (nonatomic, assign) BOOL                               needsDataRefetch;
@property (nonatomic, assign) BOOL                               canRefresh;

-(void)loadReceipts;
-(void)orderReceiptsData:(NSMutableArray*)data;
- (IBAction)flipAction:(id)sender;
-(void)applicationEnteredForeground;

-(void) adjustViewForUploadBanner;
-(void) adjustViewForNoUploadBanner;
@end
