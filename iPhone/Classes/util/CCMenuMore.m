//
//  CCMenuMore.m
//  ConcurMobile
//
//  Created by laurent mery on 13/10/2014.
//  Copyright (c) 2014 Concur. All rights reserved.
//

#import "CCMenuMore.h"
#import "UIColor+ConcurColor.h"
@class UINavigationBar;

@interface CCMenuMore() <UITableViewDelegate, UITableViewDataSource, UIGestureRecognizerDelegate>

@property (strong, nonatomic) UIView *modalView;
@property (copy, nonatomic) NSArray *menuItems;
@property (strong, nonatomic) UITapGestureRecognizer *recognizer;
@property (retain, nonatomic) UIViewController *viewController;
@property (assign, nonatomic) NSInteger defaultHeightItem;

@end

@implementation CCMenuMore

@synthesize tableViewMenu;

//public
-(id)initWithViewController:(UIViewController *)viewcontroller withMenuItems:(NSArray *)menuItems{
	
	if (self = [super init]){
		
		//init
		_viewController = viewcontroller;
		_menuItems = menuItems;
		_defaultHeightItem = 52;
		CGFloat topMenu = [self topMenu];
		NSInteger heightTableViewMenu = [self heightForAllItems];
		
		//display background modal view
		_modalView = [[UIView alloc]initWithFrame:CGRectMake(0, topMenu + heightTableViewMenu, _viewController.view.bounds.size.width, _viewController.view.bounds.size.height)];
		[_modalView setOpaque:NO];
		[_modalView setBackgroundColor:[UIColor colorWithWhite:0.0f alpha:0.6f]];
		[_viewController.view addSubview:_modalView];
		
		//display menu
		tableViewMenu = [[UITableView alloc]init];
		tableViewMenu.frame = CGRectMake(0, topMenu, _viewController.view.bounds.size.width, heightTableViewMenu);
		tableViewMenu.dataSource = self;
		tableViewMenu.delegate = self;
		tableViewMenu.autoresizingMask = UIViewAutoresizingFlexibleHeight | UIViewAutoresizingFlexibleWidth;
		[tableViewMenu registerClass:[UITableViewCell class] forCellReuseIdentifier:@"ccCellMenu"];
		[tableViewMenu reloadData];
		
		//recognize a tap outside menu to close it (tap on modal view)
		_recognizer = [[UITapGestureRecognizer alloc]initWithTarget:self action:@selector(handleTapOutside:)];
		[_recognizer setNumberOfTapsRequired:1];
		[_recognizer setCancelsTouchesInView:NO];
		_recognizer.delegate=self;
	}
	return self;
}


#pragma mark - update view

-(CGFloat)topMenu{

	// check if view has a navigation bar or not
	return 64;
}

-(NSInteger)heightForItem:(NSDictionary*)item{
	
	NSInteger height = _defaultHeightItem;
	if ([item objectForKey:@"height"] != nil){
		
		height = [item objectForKey:@"height"];
	}
	
	return height;
}

-(NSInteger)heightForAllItems{
	
	NSInteger height = 0;
	
	for (NSDictionary *item in _menuItems) {
		
		height += [self heightForItem:item];
	}
	
	return height;
}

//public
-(void)setHidden:(BOOL)hide{

    if (hide) {
        
        [tableViewMenu.window removeGestureRecognizer:_recognizer];
    }
    else {
        
        [_viewController.view.window addGestureRecognizer:_recognizer];
    }
    
	[_modalView setHidden:hide];
	[tableViewMenu setHidden:hide];
}

#pragma mark - TableView

-(NSInteger)tableView:(UITableView *)tableView numberOfRowsInSection:(NSInteger)section{
	
	return [_menuItems count];
}

-(UITableViewCell *)tableView:(UITableView *)tableView cellForRowAtIndexPath:(NSIndexPath *)indexPath{
	
	UITableViewCell *cell;
	
	cell = [[UITableViewCell alloc]initWithStyle:UITableViewCellStyleDefault reuseIdentifier:@"ccCellMenu"];
    cell.selectionStyle = UITableViewCellSelectionStyleNone;
	
	NSDictionary *item = [_menuItems objectAtIndex:indexPath.item];
	
	[cell.textLabel setText:[item objectForKey:@"title"]];
    [cell.textLabel setTextColor:[UIColor textMenuTitle]];
	[cell.textLabel setFont:[UIFont fontWithName:@"HelveticaNeue" size:18.0]];

	
	if ([item objectForKey:@"imageKey"] != nil){
		
		cell.imageView.image = [UIImage imageNamed:[item objectForKey:@"imageKey"]];
	}
	
	
	return cell;
}

//private
- (CGFloat)tableView:(UITableView *)tableView heightForRowAtIndexPath:(NSIndexPath *)indexPath{
	
	return [self heightForItem:[_menuItems objectAtIndex:indexPath.item]];
}


-(void)tableView:(UITableView *)tableView didSelectRowAtIndexPath:(NSIndexPath *)indexPath{
    
    NSDictionary *item = [_menuItems objectAtIndex:indexPath.item];
    
    [_viewController performSegueWithIdentifier:[item objectForKey:@"segueIdentifier"] sender:_viewController];
}


#pragma mark - GestureRecognizer to detect tap outside menu (in modal view ; to hide menu)


-(BOOL)gestureRecognizer:(UIGestureRecognizer *)gestureRecognizer shouldRecognizeSimultaneouslyWithGestureRecognizer:(UIGestureRecognizer *)otherGestureRecognizer{
	
	return YES;
}


-(void)handleTapOutside:(UITapGestureRecognizer*)sender{
	
	if (sender.state == UIGestureRecognizerStateEnded) {
		
		UIView *rootView = _modalView.window.rootViewController.view;
		CGPoint location = [sender locationInView:rootView];
		
		if ([_modalView pointInside:[_modalView convertPoint:location fromView:rootView] withEvent:nil]){
			
            [self setHidden:YES];
		}
		
	}
}


#pragma mark - Memory managment

-(void)dealloc{
	
	_defaultHeightItem = nil;
	_recognizer.delegate = nil;
	tableViewMenu.dataSource = nil;
	tableViewMenu.delegate = nil;
}

@end
