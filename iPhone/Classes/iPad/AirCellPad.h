//
//  AirCellPad.h
//  ConcurMobile
//
//  Created by Paul Kramer on 5/21/10.
//  Copyright 2010 Concur. All rights reserved.
//

#import <UIKit/UIKit.h>
#import "ExSystem.h" 

#import "DetailViewController.h"

@interface AirCellPad : UITableViewCell {
	UILabel				*lblDepartAirport, *lblArriveAirport, *lblDepartTime, *lblArriveTime, *lblDepartAMPM, *lblArriveAMPM, *lblDepartTerminalGate, *lblArriveTerminalGate;
	UILabel				*lblArrive;
	UIButton			*btnDepartAirport, *btnArriveAirport, *btnDepartTerminalGate, *btnArriveTerminalGate;
	
	NSString			*urlDepartAir, *urlArriveAir, *urlDepartTermGate, *urlArriveTermGate;
	RootViewController	*rootVC;
	DetailViewController	*dVC;
}

@property (strong, nonatomic) IBOutlet UILabel				*lblDepartAirport;
@property (strong, nonatomic) IBOutlet UILabel				*lblArriveAirport;
@property (strong, nonatomic) IBOutlet UILabel				*lblDepartTime;
@property (strong, nonatomic) IBOutlet UILabel				*lblArriveTime;
@property (strong, nonatomic) IBOutlet UILabel				*lblDepartAMPM;
@property (strong, nonatomic) IBOutlet UILabel				*lblArriveAMPM;
@property (strong, nonatomic) IBOutlet UILabel				*lblDepartTerminalGate;
@property (strong, nonatomic) IBOutlet UILabel				*lblArriveTerminalGate;
@property (strong, nonatomic) IBOutlet UILabel				*lblArrive;

@property (strong, nonatomic) IBOutlet UIButton			*btnDepartAirport;
@property (strong, nonatomic) IBOutlet UIButton			*btnArriveAirport;
@property (strong, nonatomic) IBOutlet UIButton			*btnDepartTerminalGate;
@property (strong, nonatomic) IBOutlet UIButton			*btnArriveTerminalGate;

@property (strong, nonatomic) NSString			*urlDepartAir;
@property (strong, nonatomic) NSString			*urlArriveAir;
@property (strong, nonatomic) NSString			*urlDepartTermGate;
@property (strong, nonatomic) NSString			*urlArriveTermGate;
@property (strong, nonatomic) RootViewController	*rootVC;
@property (strong, nonatomic) DetailViewController	*dVC;

-(IBAction)loadWebView:(id)sender;
@end
