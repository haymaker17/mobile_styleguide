//
//  ActiveRequestHeader.m
//  ConcurMobile
//
//  Created by Laurent Mery on 14/10/2014.
//  Copyright (c) 2014 Concur. All rights reserved.
//

#import "ActiveRequestHeader.h"
#import "ActiveRequestHeaderForm.h"

#import "CTETravelRequest.h"
#import "WaitViewController.h"

#import "FFFormProtocol.h"

@interface ActiveRequestHeader () <FFFormProtocol>

@property (strong, nonatomic) UIBarButtonItem *navButtonSave;

@property (weak, nonatomic) IBOutlet UIView *viewHeader;
@property (weak, nonatomic) IBOutlet UILabel *labelName;

@property (weak, nonatomic) IBOutlet UITableView *tableViewForm;
@property (nonatomic, strong) ActiveRequestHeaderForm *headerFormFields;

@end

@implementation ActiveRequestHeader

@synthesize request = _request;

static NSString* const _fluryActionLoadDatas = @"load form&fields";


+(NSString*)viewName{
	
	return @"Requests-Header";
}

- (void)viewDidLoad {
	
	[self updateNavigationBar];	
	[self initElementsSetHidden:YES];
	
    [super viewDidLoad];
	
    [self applyConcurStyle];
    [self applyLocalize];
	
	[self updateDatas];
}

#pragma mark - update view

-(void)updateNavigationBar{
	
	
	UIBarButtonItem *backButton = [[UIBarButtonItem alloc] initWithTitle:@"" style:UIBarButtonItemStylePlain target:nil action:nil];
	[self.navigationItem setBackBarButtonItem:backButton];
	[self.navigationItem setHidesBackButton:YES];
	UIBarButtonItem *navButton = [[UIBarButtonItem alloc] initWithTitle:[@"Close" localize] style:UIBarButtonItemStylePlain target:self action:@selector(goBack:)];
	[self.navigationItem setLeftBarButtonItem:navButton];
}


-(void)applyConcurStyle{
    
    [_viewHeader setBackgroundColor:[UIColor backgroundTopHeader]];
    [_labelName setTextColor:[UIColor whiteConcur]];
}


-(void)initElementsSetHidden:(BOOL)hidden{
	
	[_viewHeader setHidden:hidden];
	[_tableViewForm setHidden:hidden];
}


-(void)applyLocalize{
    
    self.navigationItem.title = [@"RequestHeader" localize];
}


#pragma mark - Navigation


-(void)goBack:(id)sender{
	
	[self.navigationController popViewControllerAnimated:YES];
}


- (void)navButtonSaveTapped:(id)sender {
	
	//TODO: save action
	NSLog(@"Save button tapped");
}


#pragma mark - Datas


-(void)updateDatas{
	
	[WaitViewController showWithText:@"" animated:YES];
	[CCFlurryLogs flurryLogSpinnerStartTimefrom:[self viewName] action:_fluryActionLoadDatas];
	
	_headerFormFields = [[ActiveRequestHeaderForm alloc] initWithTableView:_tableViewForm];
	_headerFormFields.delegate = self;

	
	dispatch_async(dispatch_get_global_queue(DISPATCH_QUEUE_PRIORITY_DEFAULT, 0), ^{
		
		[_headerFormFields initFormWithDatas:_request];
		
	dispatch_sync(dispatch_get_main_queue(), ^{ //(main thread)
			
			[_labelName setText:_request.Name];
			[self initElementsSetHidden:NO];
			[_headerFormFields refresh];
			
			[WaitViewController hideAnimated:YES withCompletionBlock:nil];
			[CCFlurryLogs flurryLogSpinnerStopTimefrom:[self viewName] action:_fluryActionLoadDatas parameters:nil];
		});
	});
}

-(void)becomeDirty{
	
	UIBarButtonItem *buttonSave = [[UIBarButtonItem alloc] initWithTitle:[@"Save" localize]
															 style:UIBarButtonItemStylePlain
															target:self
															action:@selector(navButtonSaveTapped:)];
	[self.navigationItem setRightBarButtonItem:buttonSave animated:NO];
}



-(void)pushEditViewController:(UIViewController *)editViewController{
	
	[self.navigationController pushViewController:editViewController animated:NO];
}

	

#pragma mark - memory management


- (void)dealloc{
	
	_request = nil;
	
	[CCFlurryLogs flurryLogEventReturnFromView:[ActiveRequestHeader viewName]
									to:self.callerViewName
							parameters:nil];
}


@end
