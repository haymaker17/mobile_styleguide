//
//  RequestSegmentVC.m
//  ConcurMobile
//
//  Created by Laurent Mery on 04/11/2014.
//  Copyright (c) 2014 Concur. All rights reserved.
//

#import "RequestSegmentVC.h"
#import "RequestSegmentForm.h"
#import "CTETravelRequestEntry.h"

#import "FFFormProtocol.h"

@interface RequestSegmentVC () <FFFormProtocol>

@property (weak, nonatomic) IBOutlet UIView *viewToolBarContainer;
@property (weak, nonatomic) IBOutlet UIToolbar *toolbar;
@property (weak, nonatomic) IBOutlet UIBarButtonItem *tbButtonOneWay;
@property (weak, nonatomic) IBOutlet UIBarButtonItem *tbButtonRoundTrip;
@property (weak, nonatomic) IBOutlet UIBarButtonItem *tbButtonMultiSegment;
@property (weak, nonatomic) IBOutlet UIView *viewToolBarAddSegment;
@property (weak, nonatomic) IBOutlet UIButton *tbButtonAddSegment;

@property (weak, nonatomic) IBOutlet UITableView *tableViewForm;
@property (nonatomic, strong) RequestSegmentForm *segmentFormFields;

@end

@implementation RequestSegmentVC


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
    
	//defined for next screen
    UIBarButtonItem *backButton = [[UIBarButtonItem alloc] initWithTitle:@"" style:UIBarButtonItemStylePlain target:nil action:nil];
    [self.navigationItem setBackBarButtonItem:backButton];
    [self.navigationItem setHidesBackButton:YES];
    UIBarButtonItem *navButton = [[UIBarButtonItem alloc] initWithTitle:[@"Cancel" localize] style:UIBarButtonItemStylePlain target:self action:@selector(goBack:)];
    [self.navigationItem setLeftBarButtonItem:navButton];
}


-(void)applyConcurStyle{
    
    [_viewToolBarContainer setBackgroundColor:[UIColor whiteConcur]];
    [_toolbar setBackgroundColor:[UIColor whiteConcur]];
    
    [_viewToolBarAddSegment setBackgroundColor:[UIColor backgroundToolBar]];
    [_tbButtonAddSegment setTitleColor:[UIColor textToolBarButton] forState:UIControlStateNormal];
}


-(void)initElementsSetHidden:(BOOL)hidden{
    
    [_viewToolBarContainer setHidden:hidden];
    [_viewToolBarAddSegment setHidden:hidden];
    [_tableViewForm setHidden:hidden];
}


-(void)applyLocalize{
    
    [self.navigationItem setTitle:[@"Segment" localize]];
    [self.tbButtonOneWay setTitle:[@"oneWay" localize]];
    [self.tbButtonRoundTrip setTitle:[@"roundTrip" localize]];
    [self.tbButtonMultiSegment setTitle:[@"multiSegment" localize]];
    [self.tbButtonAddSegment setTitle:[@"addSegment" localize] forState:UIControlStateNormal];
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
    [CCFlurryLogs flurryLogSpinnerStartTimefrom:[self viewName] action:@"Load Segment"];
    
    _segmentFormFields = [[RequestSegmentForm alloc] initWithTableView:_tableViewForm andDelegate:self];
    
    
    dispatch_async(dispatch_get_global_queue(DISPATCH_QUEUE_PRIORITY_DEFAULT, 0), ^{
        
        [_segmentFormFields initFormWithDatas:_entry.Segments];
        
        dispatch_sync(dispatch_get_main_queue(), ^{ //(main thread)
            
            [self initElementsSetHidden:NO];
            [_segmentFormFields refreshAll];
            
            [WaitViewController hideAnimated:YES withCompletionBlock:nil];
            [CCFlurryLogs flurryLogSpinnerStopTimefrom:[self viewName] action:@"Load Segment" parameters:nil];
        });
    });
}

#pragma mark - protocol FFFormProtocol


-(void)pushEditViewController:(UIViewController *)editViewController{
    
    [self.navigationController pushViewController:editViewController animated:NO];
}

- (void)dealloc{
	
	_request = nil;
	_entry = nil;
}

@end
