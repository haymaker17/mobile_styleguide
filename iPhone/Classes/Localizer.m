//
//  Localizer.m
//  ConcurMobile
//
//  Created by Manasee Kelkar on 9/15/10.
//  Copyright 2010 Concur. All rights reserved.
//

#import "Localizer.h"
#import "Config.h"

static Localizer *sharedInstance;

@interface Localizer ()

@end

@implementation Localizer
@synthesize configDict; //This will have different values depending upon the locale 
//@synthesize defaultDict; //This will point to the default (English) en_Strings.plist 

+(Localizer*)sharedInstance
{
    static dispatch_once_t onceToken;
    dispatch_once(&onceToken, ^{
        sharedInstance = [[Localizer alloc] init];
    });
	return sharedInstance;
}

-(Localizer*)init
{
    self = [super init];
	if (self)
	{
        NSString *preferredLang = [Localizer getPreferredLanguage];

        NSString *preferredLocale = [[NSLocale currentLocale] localeIdentifier];
        preferredLocale = [preferredLocale stringByReplacingOccurrencesOfString:@"_"
                                                                     withString:@"-"];

        
		BOOL isPseudoLoc = FALSE;
		
#ifdef TEST_LOCALIZATION
		if ([preferredLang isEqualToString:@"fi"] && [preferredLocale isEqualToString:@"ru-RU"]) 
		{
			isPseudoLoc = TRUE;
		}
#endif		
		// Create array of fallback languages
		NSArray	*fallbacks = nil;
		if (isPseudoLoc) 
		{
			fallbacks = @[@"en"];
		}
		else 
		{
			fallbacks = @[preferredLocale,preferredLang,@"en"];
		}

		NSString *path = [[NSBundle mainBundle] bundlePath];
		self.configDict = [[NSMutableDictionary alloc] init];
		
		for (NSString *prefix in fallbacks) 
		{
			NSString *filename = [NSString stringWithFormat:@"%@_Strings.plist", prefix];
			NSString *filePath = [path stringByAppendingPathComponent:filename];
				
			NSDictionary *temp = [NSDictionary dictionaryWithContentsOfFile:filePath];
			
			if (temp != nil) 
			{
				if ([configDict count] <= 0) 
				{
					[configDict addEntriesFromDictionary:temp];
				}
				else 
				{
					NSEnumerator *enumerator = [temp keyEnumerator];
					for(NSString *aKey in enumerator)
					{
						if (configDict[aKey] == nil) 
						{
							configDict[aKey] = temp[aKey];
						}	
					}
					
				}//else
			}//if
		}//for
        
        if ([Config isGov])
        {
            NSString *filePath = [path stringByAppendingPathComponent:@"en_Gov_Strings.plist"];
            
            NSDictionary *temp = [NSDictionary dictionaryWithContentsOfFile:filePath];
            
            if (temp != nil)
            {
                NSEnumerator *enumerator = [temp keyEnumerator];
                for(NSString *aKey in enumerator)
                {
                    if ([configDict objectForKey:aKey] == nil)
                    {
                        [configDict setObject:[temp objectForKey:aKey] forKey:aKey];
                    }
                }
            }
        }

		if (isPseudoLoc) 
		{
			NSMutableDictionary *tempConfig = [[NSMutableDictionary alloc] initWithDictionary:configDict];
			NSEnumerator *enumerator = [tempConfig keyEnumerator];
			for(NSString *aKey in enumerator)
			{
				NSString *val = configDict[aKey];
				val = [val stringByReplacingOccurrencesOfString:val 
						   withString:[NSString stringWithFormat:@"¥Ã放עִम%@",val]];
				configDict[aKey] = val;
			}
			
		}
	}
	return self;
}

+(NSString *)getViewTitle:(NSString *)theViewName
{
	//gets the localized title for the view 
	NSDictionary *config = [[Localizer sharedInstance] configDict];
	NSString *localViewName;
	localViewName = config[theViewName];
	
	if (localViewName == nil) 
	{
		localViewName = theViewName;
	}
	return localViewName;
}

+(NSString *)getLocalizedText:(NSString *)localConstant
{
	NSDictionary *config = [[Localizer sharedInstance] configDict];
	
	NSString *foundVal = config[localConstant];
	
	if (foundVal == nil) 
	{
        // dev build (enterprise is a dev build), let the missing string be known
        if( [Config isDevBuild] )
        {
          foundVal = @"Localized string not available";
        }
        else    // production build, hide the missing string as much as possible
        {
            foundVal = localConstant;
        }
	}
	return foundVal;
}

+(BOOL)hasLocalizedText:(NSString *)localConstant
{
	NSDictionary *config = [[Localizer sharedInstance] configDict];
	
	NSString *foundVal = config[localConstant];
	
	return (foundVal != nil);
}

//MOB-16368 IMP : Please note this same method is used to send the preferred language to MWS, This was done to keep client and MWS localization in sync.
//
+(NSString *)getPreferredLanguage
{
    NSUserDefaults* defs = [NSUserDefaults standardUserDefaults];
    NSArray* languages = [defs objectForKey:@"AppleLanguages"];
    NSString* preferredLang = languages[0];
    
    if ([@"zh-Hans" isEqualToString:preferredLang])
        preferredLang = @"zh-cn";
    else if ([@"zh-Hant" isEqualToString:preferredLang])
        preferredLang = @"zh-tw";
    if ([@"pt" isEqualToString:preferredLang])
        preferredLang = @"pt-BR";
    
    // MOB-14096 : take out Czech, Russian, and Greek
    // MOB-18738 : Add Russian support (Pfizer)
    if ([@"el" isEqualToString:preferredLang] || [@"cs" isEqualToString:preferredLang] || [@"da" isEqualToString:preferredLang] || [@"fi" isEqualToString:preferredLang] || [@"ko" isEqualToString:preferredLang] || [@"lt" isEqualToString:preferredLang] || [@"lv" isEqualToString:preferredLang] || [@"no" isEqualToString:preferredLang] || [@"pl" isEqualToString:preferredLang] || [@"sk" isEqualToString:preferredLang] || [@"tr" isEqualToString:preferredLang] || [@"zh-tw" isEqualToString:preferredLang])
        preferredLang = @"en";

    if ([Config isGov])
    {
        preferredLang = @"en";
    }
    
    return preferredLang;
}

@end
