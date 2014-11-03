//
//  FlightStatsViewController.h
//  ConcurMobile
//
//  Created by Charlotte Fallarme on 9/1/10.
//  Copyright 2010 Concur. All rights reserved.
//

#import <UIKit/UIKit.h>
#import "MobileViewController.h"
#import "ItinDetailCell.h"
#import "EntitySegment.h"

@interface FlightStatsViewController : MobileViewController
{
	UIToolbar				*tBar;
	UIView					*fetchView;
	UIActivityIndicatorView	*spinnerFetch;
	UITableView				*tableList;
	NSString				*tripKey;
	NSString				*segmentKey;
	EntitySegment			*segment;
	NSMutableArray			*sections;
	UINavigationBar			*navBar;
	UIBarButtonItem			*doneBtn;
	UILabel					*lblLoading;
}

@property (nonatomic, strong) IBOutlet UIToolbar				*tBar;
@property (nonatomic, strong) IBOutlet UIView					*fetchView;
@property (nonatomic, strong) IBOutlet UIActivityIndicatorView	*spinnerFetch;
@property (nonatomic, strong) IBOutlet UITableView				*tableList;
@property (nonatomic, strong) NSString							*tripKey;
@property (nonatomic, strong) NSString							*segmentKey;
@property (nonatomic, strong) EntitySegment						*segment;
@property (nonatomic, strong) NSMutableArray					*sections;
@property (nonatomic, strong) IBOutlet UINavigationBar			*navBar;
@property (nonatomic, strong) IBOutlet UIBarButtonItem			*doneBtn;
@property (nonatomic, strong) IBOutlet UILabel					*lblLoading;
-(IBAction) donePressed:(id)sender;

-(void) makeRefreshButton:(NSString *)dtRefreshed;
-(void) refreshData;

- (NSString*)notApplicableIfNil:(NSString*)str;

//-(NSInteger)sectionIdentifierFromSection:(NSInteger)section;

-(void) fillAirSections;

-(void)goSomeplace:(NSString *)mapAddress VendorName:(NSString *)vendorName VendorCode:(NSString *)vendorCode;
-(IBAction)loadWebViewSeat:(NSString *)specialValueWeb WebViewTitle:(NSString *)webViewTitle;
-(IBAction)loadWebView:(NSString *)specialValueWeb WebViewTitle:(NSString *)webViewTitle;

@end
