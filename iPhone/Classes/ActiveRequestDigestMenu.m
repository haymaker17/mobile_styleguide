//
//  ActiveRequestDigestMenu.m
//  ConcurMobile
//
//  Created by laurent mery on 13/10/2014.
//  Copyright (c) 2014 Concur. All rights reserved.
//

#import "ActiveRequestDigestMenu.h"
#import "UIColor+ConcurColor.h"

@interface ActiveRequestDigestMenu() <UITableViewDelegate, UITableViewDataSource, UIGestureRecognizerDelegate>

@property (strong, nonatomic) UIView *modalView;
@property (strong, nonatomic) UITapGestureRecognizer *recognizer;
@property (retain, nonatomic) UIView *view;

@end

@implementation ActiveRequestDigestMenu

@synthesize tableViewMenu;

-(id)initOnView:(UIView*)view{
	
	if (self = [super init]){
		
		_view = view;
		
		//display background modal view
		_modalView = [[UIView alloc]initWithFrame:CGRectMake(0,64+52, view.bounds.size.width, view.bounds.size.height)];
		[_modalView setOpaque:NO];
		[_modalView setBackgroundColor:[UIColor colorWithWhite:0.0f alpha:0.6f]];
		[view addSubview:_modalView];
		
		//display menu
		tableViewMenu = [[UITableView alloc]init];
		tableViewMenu.frame = CGRectMake(0, 64, view.bounds.size.width, 52);
		tableViewMenu.dataSource = self;
		tableViewMenu.delegate = self;
		tableViewMenu.autoresizingMask = UIViewAutoresizingFlexibleHeight | UIViewAutoresizingFlexibleWidth;
		[tableViewMenu registerClass:[UITableViewCell class] forCellReuseIdentifier:@"CellMenu"];
		[tableViewMenu reloadData];
		
		//recognize a clic outside menu to close it
		_recognizer = [[UITapGestureRecognizer alloc]initWithTarget:self action:@selector(handleTapOutside:)];
		[_recognizer setNumberOfTapsRequired:1];
		[_recognizer setCancelsTouchesInView:NO];
		_recognizer.delegate=self;
	}
	return self;
}

-(void)show{
	
	[_view.window addGestureRecognizer:_recognizer];
	[_modalView setHidden:NO];
	[tableViewMenu setHidden:NO];
}

-(void)hide{
	
	[tableViewMenu.window removeGestureRecognizer:_recognizer];
	[_modalView setHidden:YES];
	[tableViewMenu setHidden:YES];
}

-(NSInteger)tableView:(UITableView *)tableView numberOfRowsInSection:(NSInteger)section{
	
	return 1;
}

-(UITableViewCell *)tableView:(UITableView *)tableView cellForRowAtIndexPath:(NSIndexPath *)indexPath{
	
	UITableViewCell *cell;
	
	cell = [[UITableViewCell alloc]initWithStyle:UITableViewCellStyleDefault reuseIdentifier:@"CellMenu"];
	
	cell.textLabel.text = [@"RequestHeader" localize];
	cell.imageView.image = [UIImage imageNamed:@"iconblue_request"];
	
	
	return cell;
}

//private
- (CGFloat)tableView:(UITableView *)tableView heightForRowAtIndexPath:(NSIndexPath *)indexPath{
	
	return 52;
}

//private (remove the footer ; 0 doesn't work)
- (CGFloat)tableView:(UITableView *)tableView heightForFooterInSection:(NSInteger)section{
	
	return 0.1;
}

-(BOOL)gestureRecognizer:(UIGestureRecognizer *)gestureRecognizer shouldRecognizeSimultaneouslyWithGestureRecognizer:(UIGestureRecognizer *)otherGestureRecognizer{
	
	return YES;
}


-(void)handleTapOutside:(UITapGestureRecognizer*)sender{
	
	if (sender.state == UIGestureRecognizerStateEnded) {
		
		UIView *rootView = _modalView.window.rootViewController.view;
		CGPoint location = [sender locationInView:rootView];
		
		if ([_modalView pointInside:[_modalView convertPoint:location fromView:rootView] withEvent:nil]){
			
			[self hide];
		}
		
	}
}

-(void)dealloc{
	
	_recognizer.delegate = nil;
	tableViewMenu.dataSource = nil;
	tableViewMenu.delegate = nil;
}

@end
