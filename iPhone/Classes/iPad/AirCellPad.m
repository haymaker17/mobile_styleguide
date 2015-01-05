//
//  AirCellPad.m
//  ConcurMobile
//
//  Created by Paul Kramer on 5/21/10.
//  Copyright 2010 Concur. All rights reserved.
//

#import "AirCellPad.h"


@implementation AirCellPad
@synthesize lblDepartAirport, lblArriveAirport, lblDepartTime, lblArriveTime, lblDepartAMPM, lblArriveAMPM, lblDepartTerminalGate, lblArriveTerminalGate, lblArrive;
@synthesize btnDepartAirport, btnArriveAirport, btnDepartTerminalGate, btnArriveTerminalGate;
@synthesize urlDepartAir, urlArriveAir, urlDepartTermGate, urlArriveTermGate;
@synthesize rootVC;
@synthesize dVC;

- (id)initWithStyle:(UITableViewCellStyle)style reuseIdentifier:(NSString *)reuseIdentifier {
    if ((self = [super initWithStyle:style reuseIdentifier:reuseIdentifier])) {
        // Initialization code
    }
    return self;
}


- (void)setSelected:(BOOL)selected animated:(BOOL)animated {

    [super setSelected:selected animated:animated];

    // Configure the view for the selected state
}




-(IBAction)loadWebView:(id)sender
{

	UIButton *btn = (UIButton *)sender;
	NSInteger aPos = btn.tag;

	WebViewController *webView = [[WebViewController alloc] init];
	webView.rootViewController = rootVC;
	
	if(aPos == 10)
	{//depart air
		webView.url = [NSString stringWithFormat:@"http://%@", urlDepartAir];
		webView.viewTitle =lblDepartAirport.text;
	}
	else if(aPos == 20)
	{//depart term gate
		webView.url = [NSString stringWithFormat:@"http://%@", urlDepartTermGate];
		webView.viewTitle = lblDepartTerminalGate.text;
	}
	else if(aPos == 30)
	{//arrive air
		webView.url = [NSString stringWithFormat:@"http://%@", urlArriveAir];
		webView.viewTitle = lblArriveAirport.text;
	}
	else if(aPos == 40)
	{//arrive term gate
		webView.url = [NSString stringWithFormat:@"http://%@", urlArriveTermGate];
		webView.viewTitle = lblArriveTerminalGate.text;
	}
#if __IPHONE_OS_VERSION_MAX_ALLOWED >= 30200
	webView.modalPresentationStyle = UIModalPresentationFormSheet;
#endif
	[dVC presentViewController:webView animated:YES completion:nil]; 
	
}

@end
